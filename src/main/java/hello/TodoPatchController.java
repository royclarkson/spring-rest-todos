package hello;

import hello.jsonpatch.JsonPatch;
import hello.jsonpatch.JsonPatchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/todos")
public class TodoPatchController {

	private final ObjectMapper objectMapper = new ObjectMapper(); //.setSerializationInclusion(Include.NON_NULL);

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
	@RequestMapping(
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<String> patch(JsonPatch jsonPatch, HttpSession session) 
			throws JsonPatchException, IOException {
		
		// get shadow from session / calculate it if it isn't in session 
		List<Todo> shadow = (List<Todo>) shadowStore.getShadow("/todos/shadow");

		// we need the full list because that's what this resource is and because it's
		// what we'll use to compare with the shadow to find any changes we need to
		// send back to the client in a patch...so we need this resource.
		// Unfortunately, it's a database hit (or a cache hit) to do it.
		// There's really no way around this, but perhaps caching around the repository
		// will make it better.
		List<Todo> allTodos = (List<Todo>) getEntityList();
		
		// get source as JsonNode of all Todo items
		List<Todo> source = deepCloneList(allTodos);
		if (shadow == null) {
			shadow = deepCloneList(source);
		}

		// Don't bother applying patch if there's nothing in the patch.
		if (jsonPatch.size() > 0) {
	
			// apply patch to shadow
			shadow = (List<Todo>) jsonPatch.apply(shadow);
	
			// apply patch to source
			source = (List<Todo>) jsonPatch.apply(source);
	
	
			// convert patched source back to a set of actual Todo items
			System.out.println(source.getClass().getName());
			List<Todo> patchedTodos = deepCloneList(source);
			
			
			// determine which items are modified/added and should be saved
			List<Todo> itemsToSave = new ArrayList<Todo>(patchedTodos);
			System.out.println("BEFORE:  " + itemsToSave);
			
			itemsToSave.removeAll(allTodos);
			
			// save the modified/added items
			if (itemsToSave.size() > 0) {
				System.out.println("AFTER:  " + itemsToSave);
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
		}
		
		
		
		// Up to this point, we've focused on applying the client-sent patch to the
		// shadow.
		// Now it's time to work the other direction, calculating the difference
		// between the source and the shadow so we can communicate those changes to
		// the client (and then updating the shadow accordingly).
		
		
		// TODO: Implement diff so that we can uncomment the following lines
		
//		// diff shadow against source to calcuate returnPatch
//		JsonNode returnPatch = JsonDiff.asJson(shadow, source);
//		
//		// apply return patch to shadow
//		shadow = JsonPatch.fromJson(returnPatch).apply(shadow);
//		
//		// update session with new shadow
//		shadowStore.putShadow("/todos/shadow", shadow);
		
		// return returnPatch
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json-patch+json"));
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("[]", headers, HttpStatus.OK);
		
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
