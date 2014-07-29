package hello;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.patch.diffsync.DiffSync;
import org.springframework.web.patch.diffsync.PersistenceStrategy;
import org.springframework.web.patch.diffsync.ShadowStore;
import org.springframework.web.patch.jsonpatch.JsonPatch;
import org.springframework.web.patch.jsonpatch.JsonPatchException;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/todos")
public class TodoPatchController {

	private TodoRepository todoRepository;

	// Should be session-scoped or otherwise unique to each client
	private ShadowStore shadowStore;
	
	@Autowired
	public TodoPatchController(TodoRepository todoRepository, ShadowStore shadowStore) {
		this.todoRepository = todoRepository;
		this.shadowStore = shadowStore;
	}
	
	@RequestMapping(
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<JsonNode> patch(JsonPatch jsonPatch) throws JsonPatchException, IOException, Exception {

		PersistenceStrategy<List<Todo>> persistence = new PersistenceStrategy<List<Todo>>() {
			@Override
			public List<Todo> save(List<Todo> t) {
				return (List<Todo>) todoRepository.save(t);
			}
			
			@Override
			public List<Todo> find() {
				return (List<Todo>) todoRepository.findAll();
			}
			
			@Override
			public void delete(List<Todo> t) {
				todoRepository.delete(t);
			}
		};
		DiffSync<Todo> sync = new DiffSync<Todo>(jsonPatch, shadowStore, persistence, Todo.class);
		
		JsonNode returnPatch = sync.apply();

		// return returnPatch
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json-patch+json"));
		ResponseEntity<JsonNode> responseEntity = new ResponseEntity<JsonNode>(returnPatch, headers, HttpStatus.OK);
		
		return responseEntity;
	}

}
