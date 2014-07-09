package hello;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

@RestController
@RequestMapping("/todos")
public class TodoPatchController {

	private final ObjectMapper objectMapper = new ObjectMapper(); //.setSerializationInclusion(Include.NON_NULL);

	private TodoRepository todoRepository;

	@Autowired
	public TodoPatchController(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}
	
	
	// TODO: Consider if Spring Session is a suitable option here or if there should instead be some sort of ShadowStore implementation
	//       rather than relying on user-centric HttpSession.
	@RequestMapping(
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<JsonNode> patch(JsonPatch jsonPatch, HttpSession session) 
			throws JsonPatchException, IOException {
		
		// TODO: if jsonPatch is empty, don't bother applying it or saving the patched items
		
		// get shadow from session / calculate it if it isn't in session 
		JsonNode shadow = (JsonNode) session.getAttribute("/todos/shadow");

		Iterable<Todo> allTodos = getEntityList();
		JsonNode source = asJsonNode(allTodos);
		if (shadow == null) {
			shadow = source;
		}

		// apply patch to shadow
		shadow = jsonPatch.apply(shadow);

		// apply patch to source
		source = jsonPatch.apply(source);

		// TODO: This is very hacky...find better way that doesn't involve baking up a new array and
		//       saving *all* items at once
		// BEGIN VERY HACKY CODE
		ArrayList<Todo> patchedTodos = new ArrayList<Todo>();
		Iterator<JsonNode> elements = source.elements();
		while(elements.hasNext()) {
			JsonNode todoNode = elements.next();
			Todo todo = objectMapper.treeToValue(todoNode, Todo.class);
			patchedTodos.add(todo);
		}
		todoRepository.save(patchedTodos);

		List<Todo> allTodosList = (List<Todo>) allTodos;
		allTodosList.removeAll(patchedTodos);
		todoRepository.delete(allTodosList);
		// END VERY HACKY CODE
		
		
		// diff shadow against source to calcuate returnPatch
		JsonNode returnPatch = JsonDiff.asJson(shadow, source);
		
		// apply return patch to shadow
		shadow = JsonPatch.fromJson(returnPatch).apply(shadow);
		
		// update session with new shadow
		session.setAttribute("/todos/shadow", shadow);
		
		// return returnPatch
		return new ResponseEntity<JsonNode>(returnPatch, HttpStatus.OK);
	}
	
	
	protected Iterable<Todo> getEntityList() {
		return todoRepository.findAll();
	}
	
	private JsonNode asJsonNode(Object o) {
		return objectMapper.convertValue(o, JsonNode.class);
	}
}
