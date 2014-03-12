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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

@RestController
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	ObjectMapper objectMapper;

	private List<Todo> todos;

	private TodoRepository repo;

	@Autowired
	public MainController(TodoRepository repo) {
		this.repo = repo;
		// initialize list
		todos = new ArrayList<Todo>();
		todos.add(new Todo("a", false));
		todos.add(new Todo("b", false));
		todos.add(new Todo("c", false));
	}

	@RequestMapping("/")
	public String home() {
		return "Todo List";
	}

	@RequestMapping(value = "/todos", method = RequestMethod.GET, produces = "application/json")
	public List<Todo> list() {
		return repo.findAll();
	}

	@RequestMapping(value = "/todos", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
	public List<Todo> patch(@RequestBody JsonNode modifiedTodos) {
		JsonPatch patch = null;
		try {
			patch = JsonPatch.fromJson(modifiedTodos);
		} catch (IOException e) {
			logger.error("Patch request is not valid JSON. Returning unmodified list.", e);
		}
		try {
			JsonNode patchedTodos = patch.apply(getTodosJson());
			updateTodosFromJson(patchedTodos);
		} catch (JsonPatchException e) {
			logger.error("Failed to apply patch! Returning unmodified list.", e);
		}
		return this.todos;
	}

	@RequestMapping(value = "/todos", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public Todo create(@RequestBody Todo todo) {
		return repo.save(todo);
	}

	@RequestMapping(value = "/todos/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(@RequestBody Todo todo, @PathVariable("id") int id) {
		repo.save(todo);
	}

	@RequestMapping(value = "/todos/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") long id) {
		repo.delete(id);
	}

	// utilities

	@RequestMapping(value = "todos/diff", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public JsonNode diff(@RequestBody JsonNode modifiedTodos) {
		return JsonDiff.asJson(getTodosJson(), modifiedTodos);
	}

	private JsonNode getTodosJson() {
		return objectMapper.convertValue(this.todos, JsonNode.class);
	}

	private void updateTodosFromJson(JsonNode todosJson) {
		Todo[] todoArray = objectMapper.convertValue(todosJson, Todo[].class);
		this.todos.clear();
		this.todos.addAll(Arrays.asList(todoArray));
	}

}
