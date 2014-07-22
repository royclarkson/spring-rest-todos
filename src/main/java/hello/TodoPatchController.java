package hello;

import hello.jsonpatch.JsonDiff;
import hello.jsonpatch.JsonPatch;
import hello.jsonpatch.JsonPatchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/todos")
public class TodoPatchController {

	private TodoRepository todoRepository;

	private ShadowStore<Object> shadowStore;
	
	private Sameness samenessTest = new IdPropertySameness(); // TODO: Inject instead of hardcode

	@Autowired
	public TodoPatchController(TodoRepository todoRepository, ShadowStore<Object> shadowStore) {
		this.todoRepository = todoRepository;
		this.shadowStore = shadowStore;
	}
	
	
	// TODO: Consider if Spring Session is a suitable option here or if there should instead be some sort of ShadowStore implementation
	//       rather than relying on user-centric HttpSession.
	@SuppressWarnings("unchecked")
	@RequestMapping(
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<JsonNode> patch(JsonPatch jsonPatch, HttpSession session) 
			throws JsonPatchException, IOException, Exception {
		
		// we need 5 copies:
		// - The original, as retrieved from the database. Remains untouched for comparison purposes.
		// - The source, cloned from the original so that when the patch is applied the original remains untouched
		// - The shadow, retrieved from the shadow store or cloned from the original if not already in the shadow store.
		// - itemsToSave, a clone of source, trimmed down to only contain the items that were added or modified
		// - itemsToRemove, a clone of the original, trimmed down to only contain items that are no longer in the source (that is, items that were deleted)
		
		List<Todo> original = (List<Todo>) getEntityList();

		List<Todo> source = deepCloneList(original);

		// get shadow from session / calculate it if it isn't in session 
//		List<Todo> shadow = (List<Todo>) shadowStore.getShadow("/todos/shadow");
		List<Todo> shadow = (List<Todo>) session.getAttribute("/todos/shadow");
		if (shadow == null) {
			shadow = deepCloneList(source);
		}

		if (jsonPatch.size() > 0) {
			shadow = (List<Todo>) jsonPatch.apply(shadow);
			source = (List<Todo>) jsonPatch.apply(source);

			List<Todo> itemsToSave = new ArrayList<Todo>(source);
			itemsToSave.removeAll(original);
			
			if (itemsToSave.size() > 0) {
				todoRepository.save(itemsToSave);
			}
	
			// REMOVE ITEMS
			List<Todo> itemsToRemove = deepCloneList(original);
			for (Todo candidate : original) {
				for (Todo todo : source) {
					if (samenessTest.isSame(candidate, todo)) {
						itemsToRemove.remove(candidate);
						break;
					}
				}
			}
			
			if (itemsToRemove.size() > 0) {
				todoRepository.delete(itemsToRemove);
			}
		}
		
		
		
		// Up to this point, we've focused on applying the client-sent patch to the
		// shadow.
		// Now it's time to work the other direction, calculating the difference
		// between the source and the shadow so we can communicate those changes to
		// the client (and then updating the shadow accordingly).
		
				
		// diff shadow against source to calcuate returnPatch
		JsonNode returnPatch = new JsonDiff().diff(shadow, source);
		
		// apply return patch to shadow
		shadow = (List<Todo>) JsonPatch.fromJsonNode(returnPatch).apply(shadow);
		
		// update session with new shadow
//		shadowStore.putShadow("/todos/shadow", shadow);
		session.setAttribute("/todos/shadow", shadow);
		
		// return returnPatch
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json-patch+json"));
		ResponseEntity<JsonNode> responseEntity = new ResponseEntity<JsonNode>(returnPatch, headers, HttpStatus.OK);
		
		return responseEntity;
	}


	protected Iterable<Todo> getEntityList() {
		return todoRepository.findAll();
	}
	
	private List<Todo> deepCloneList(List<Todo> original) {
		List<Todo> copy = new ArrayList<Todo>(original.size());
		for (Todo todo : original) {
			copy.add((Todo) SerializationUtils.clone(todo));
		}
		return copy;
	}

}
