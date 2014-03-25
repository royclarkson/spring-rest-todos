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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

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
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> patch(JsonPatch patch, @RequestHeader(value="If-Match", required=false) String ifMatch) {
		try {
			JsonNode todosJson = getTodosJson();
			String generateETagHeaderValue = generateETagHeaderValue(todosJson);
			System.out.println(generateETagHeaderValue);
			if (ifMatch == null || ifMatch.equals(generateETagHeaderValue)) {
				JsonNode patchedTodos = patch.apply(todosJson);
				updateTodosFromJson(patchedTodos);
				HttpHeaders headers = new HttpHeaders();
				headers.setETag(generateETagHeaderValue(patchedTodos));
				return new ResponseEntity<Void>(headers, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		} catch (JsonPatchException e) {
			logger.error("Failed to apply patch! Returning unmodified list.", e);
			return new ResponseEntity<Void>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
	
	protected String generateETagHeaderValue(JsonNode node) {
		byte[] bytes = node.toString().getBytes();
		StringBuilder builder = new StringBuilder("\"0");
		DigestUtils.appendMd5DigestAsHex(bytes, builder);
		builder.append("\"");
		return builder.toString();
	}

	@RequestMapping(value = "/diff", 
					method = RequestMethod.POST, 
					consumes="application/json", 
					produces = {"application/json", "application/json-patch+json"})
	public JsonNode diff(@RequestBody JsonNode modifiedTodos) {
		return JsonDiff.asJson(getTodosJson(), modifiedTodos);
	}

	
	
	// private helpers
	private JsonNode getTodosJson() {
		List<Todo> allTodos = repository.findAll();
		return objectMapper.convertValue(allTodos, JsonNode.class);
	}

	private void updateTodosFromJson(JsonNode todosJson) {
		Todo[] todoArray = objectMapper.convertValue(todosJson, Todo[].class);
		List<Todo> todoList = Arrays.asList(todoArray);
		repository.save(todoList);
	}

}
