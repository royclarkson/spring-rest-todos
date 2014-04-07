/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hello;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.PatchListenerAdapter;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * REST controllers can extend this abstract class to support handling of JSON Patch requests against a given resource type.
 * @author Craig Walls
 * @param <T> The entity type of the resource that this controller deals with.
 * @param <I> The ID type of the entity.
 */
public abstract class JsonPatchControllerSupport<T, I> {
	private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Class<T> clazz;

	public JsonPatchControllerSupport(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@RequestMapping(method=RequestMethod.PATCH, 
					consumes={"application/json", "application/json-patch+json"}, 
					produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<JsonNode> patchList(JsonPatch patch, @RequestHeader(value="If-Match", required=false) String ifMatch) throws Exception {
		PatchResult<List<T>> patchResult = performMatch(patch, ifMatch, getEntityList(), clazz);
		List<T> savedEntityList = saveEntityList(patchResult.getEntity());
		JsonNode savedEntityListJson = objectMapper.convertValue(savedEntityList, JsonNode.class);
		String etag = generateETagHeaderValue(savedEntityListJson);
		JsonNode diff = JsonDiff.asJson(patchResult.getPatchedNode(), savedEntityListJson);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json-patch+json"));
		headers.setETag(etag);
		return new ResponseEntity<JsonNode>(diff, headers, HttpStatus.OK);
	}

	@RequestMapping(
			value="/{id}",
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces = "application/json")
	public ResponseEntity<Void> patchEntity(I id, JsonPatch patch, @RequestHeader(value="If-Match", required=false) String ifMatch) throws Exception {
		PatchResult<T> patchResult = performMatch(patch, ifMatch, getEntity(id));
		T savedEntity = saveEntity(patchResult.getEntity());

		// 1. convert savedEntity to JSON
		JsonNode savedEntityJson = objectMapper.convertValue(savedEntity, JsonNode.class);
		
		// 2. calculate etag from savedEntity's JSON, *NOT* from patchResult (the saved may have IDs that the other doesn't)
    String etag = generateETagHeaderValue(savedEntityJson);

		// 3. calculate a diff between the savedEntity's JSON and the patchedEntity's JSON.
		// 4. return the diff as application/json-patch+json
		
		HttpHeaders headers = new HttpHeaders();
		headers.setETag(etag);
		return new ResponseEntity<Void>(headers, HttpStatus.NO_CONTENT);
	}
	
	@ExceptionHandler(JsonPatchException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public void handleJsonPatchException(JsonPatchException e) {
		logger.error("Failed to apply patch! Returning unmodified list.", e);
	}

	@ExceptionHandler(ETagMismatchException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public void handleETagMismatchException() {
	}
	

	// hooks for persistence
	protected abstract T getEntity(I id);
	
	protected abstract T saveEntity(T entityList);

	protected abstract List<T> getEntityList();
	
	protected abstract List<T> saveEntityList(List<T> entityList);
	
	protected abstract void deleteEntity(T entity);
	
	// private helpers
	private PatchResult<List<T>> performMatch(JsonPatch patch, String ifMatch, List<T> entity, final Class<T> listType) throws JsonPatchException, ETagMismatchException {
		JsonNode original = asJsonNodeIfMatch(entity, ifMatch);
		JsonNode patched = patch.apply(original, new PatchListenerAdapter() {
			@Override
			public void remove(JsonNode node, JsonPointer path) {
				JsonNode target = path.get(node);
				
				//
				// TODO: If I can get this into an entity form, then we can delete(entity) instead of delete(id)
				//
				T entity = null; // TODO: SOMEHOW GET THE NODE INTO AN OBJECT
				deleteEntity(entity);
			}
		});
		JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, listType);
		List<T> list = objectMapper.convertValue(patched, type);
		PatchResult<List<T>> patchResult = new PatchResult<List<T>>(list, patched);
		return patchResult;
	}

	private PatchResult<T> performMatch(JsonPatch patch, String ifMatch, T entity) throws JsonPatchException, ETagMismatchException {
		JsonNode original = asJsonNodeIfMatch(entity, ifMatch);
		JsonNode patched = patch.apply(original);
		T list = objectMapper.convertValue(patched, clazz);
		PatchResult<T> patchResult = new PatchResult<T>(list, patched);
		return patchResult;
	}

	private JsonNode asJsonNodeIfMatch(Object o, String ifMatch) throws ETagMismatchException {
		JsonNode json = objectMapper.convertValue(o, JsonNode.class);
		String etag = generateETagHeaderValue(json);
		if (ifMatch == null || ifMatch.equals(etag)) {
			return json;
		}
		throw new ETagMismatchException(ifMatch, etag);
	}
	
	private String generateETagHeaderValue(JsonNode node) {
		byte[] bytes = node.toString().getBytes();
		StringBuilder builder = new StringBuilder("\"0");
		DigestUtils.appendMd5DigestAsHex(bytes, builder);
		builder.append("\"");
		return builder.toString();
	}

	private static class PatchResult<T> {
		private final T entity;
		private final JsonNode patchedNode;

		public PatchResult(T entity, JsonNode patchedNode) {
			this.entity = entity;
			this.patchedNode = patchedNode;
		}
		
		public T getEntity() {
			return entity;
		}
		
		public JsonNode getPatchedNode() {
		  return patchedNode;
		}
	}
	
	@SuppressWarnings("serial")
	private static class ETagMismatchException extends Exception {
		public ETagMismatchException(String expected, String received) {
			super("Expected " + expected +" but received " + received + ".");
		}
	}
}