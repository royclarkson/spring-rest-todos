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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

/**
 * @author Roy Clarkson
 * @author Craig Walls
 */
@RestController
@RequestMapping("/todos")
public class PatchController {
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private TodoRepository repository;
	
	@Autowired
	public PatchController(TodoRepository repository) {
		this.repository = repository;
	}

	@RequestMapping(method=RequestMethod.PATCH, 
					consumes={"application/json", "application/json-patch+json"}, 
					produces = "application/json")
	public ResponseEntity<Void> patch(JsonPatch patch, @RequestHeader(value="If-Match", required=false) String ifMatch) throws Exception {
		PatchResult<List<Todo>> patchResult = performMatch(patch, ifMatch, repository.findAll(), Todo.class);
		repository.save(patchResult.getEntity());
		HttpHeaders headers = new HttpHeaders();
		headers.setETag(patchResult.getETag());
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
	
	// private helpers
	private <T> PatchResult<List<T>> performMatch(JsonPatch patch, String ifMatch, List<T> entity, Class<T> listType) throws JsonPatchException, ETagMismatchException {
		JsonNode todosJson = asJsonNodeIfMatch(entity, ifMatch);
		JsonNode patchedTodos = patch.apply(todosJson);
		JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, listType);
		List<T> todoList = objectMapper.convertValue(patchedTodos, type);
		PatchResult<List<T>> patchResult = new PatchResult<List<T>>(todoList, generateETagHeaderValue(patchedTodos));
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

	private JsonNode getTodosJson() {
		List<Todo> allTodos = repository.findAll();
		return objectMapper.convertValue(allTodos, JsonNode.class);
	}

	private static class PatchResult<T> {
		private final T entity;
		private final String etag;

		public PatchResult(T entity, String etag) {
			this.entity = entity;
			this.etag = etag;
		}
		
		public T getEntity() {
			return entity;
		}
		
		public String getETag() {
			return etag;
		}
	}
	
	private static class ETagMismatchException extends Exception {
		public ETagMismatchException(String expected, String received) {
			super("Expected " + expected +" but received " + received + ".");
		}
	}
}
