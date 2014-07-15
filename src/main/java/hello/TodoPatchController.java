package hello;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.collect.Sets;

@RestController
@RequestMapping("/todos")
public class TodoPatchController {

	private final ObjectMapper objectMapper = new ObjectMapper(); //.setSerializationInclusion(Include.NON_NULL);

	private TodoRepository todoRepository;

	private ShadowStore<JsonNode> shadowStore;
	
	private SamenessTest samenessTest = new IdPropertySamenessTest(); // TODO: Inject instead of hardcode

	@Autowired
	public TodoPatchController(TodoRepository todoRepository, ShadowStore<JsonNode> shadowStore) {
		this.todoRepository = todoRepository;
		this.shadowStore = shadowStore;
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
		// Q: How to know if jsonPatch is empty
		
		// get shadow from session / calculate it if it isn't in session 
		JsonNode shadow = shadowStore.getShadow("/todos/shadow");

		// we need the full list because that's what this resource is and because it's
		// what we'll use to compare with the shadow to find any changes we need to
		// send back to the client in a patch...so we need this resource.
		// Unfortunately, it's a database hit (or a cache hit) to do it.
		// There's really no way around this, but perhaps caching around the repository
		// will make it better.
		Set<Todo> allTodos = Sets.newLinkedHashSet(getEntityList());
		
		// get source as JsonNode of all Todo items
		JsonNode source = asJsonNode(allTodos);
		if (shadow == null) {
			shadow = source;
		}

		// apply patch to shadow
		shadow = jsonPatch.apply(shadow);

		// apply patch to source
		source = jsonPatch.apply(source);


		// convert patched source back to a set of actual Todo items
		Set<Todo> patchedTodos = nodeToSet(source);
		
		
		// determine which items are modified/added and should be saved
		Set<Todo> itemsToSave = new LinkedHashSet<Todo>(patchedTodos);
		itemsToSave.removeAll(allTodos);
		
		// save the modified/added items
		if (itemsToSave.size() > 0) {
			todoRepository.save(itemsToSave);
		}

		// REMOVE ITEMS
		Set<Todo> itemsToRemove = new LinkedHashSet<Todo>(allTodos);
		for (Todo candidate : allTodos) {
			for (Todo todo : patchedTodos) {
				if (samenessTest.isSame(candidate, todo)) {
					itemsToRemove.remove(candidate);
					break;
				}
			}
		}
		
		if (itemsToRemove.size() > 0) {
			todoRepository.delete(itemsToRemove);
		}
		
		
		
		// Up to this point, we've focused on applying the client-sent patch to the
		// shadow.
		// Now it's time to work the other direction, calculating the difference
		// between the source and the shadow so we can communicate those changes to
		// the client (and then updating the shadow accordingly).
		
		
		
		// diff shadow against source to calcuate returnPatch
		JsonNode returnPatch = JsonDiff.asJson(shadow, source);
		
		// apply return patch to shadow
		shadow = JsonPatch.fromJson(returnPatch).apply(shadow);
		
		// update session with new shadow
		shadowStore.putShadow("/todos/shadow", shadow);
		
		// return returnPatch
		return new ResponseEntity<JsonNode>(returnPatch, HttpStatus.OK);
	}


	private Set<Todo> nodeToSet(JsonNode source) throws JsonProcessingException {
		Set<Todo> patchedTodos;
		patchedTodos = new LinkedHashSet<Todo>(source.size());
		for(Iterator<JsonNode> elements = source.elements(); elements.hasNext();) {
			patchedTodos.add(objectMapper.treeToValue(elements.next(), Todo.class));
		}
		return patchedTodos;
	}
	
	
	protected Iterable<Todo> getEntityList() {
		return todoRepository.findAll();
	}
	
	private JsonNode asJsonNode(Object o) {
		return objectMapper.convertValue(o, JsonNode.class);
	}

}
