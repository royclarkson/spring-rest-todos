package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPatchTest {

	@Test
	public void fromJsonNode() throws Exception {
		JsonPatch patch = readJsonPatch("/hello/patch-many-successful-operations.json");
		assertEquals(5, patch.size());
		
	}

	@Test
	public void manySuccessfulOperations() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "D", false));
		todos.add(new Todo(5L, "E", false));
		todos.add(new Todo(6L, "F", false));
		
		JsonPatch patch = readJsonPatch("/hello/patch-many-successful-operations.json");
		assertEquals(5, patch.size());

		List<Todo> patchedTodos = (List<Todo>) patch.apply(todos);
		
		assertEquals(6, todos.size());
		assertTrue(todos.get(1).isComplete());
		assertEquals("C", todos.get(3).getDescription());
		assertEquals("A", todos.get(4).getDescription());
	}

	@Test
	public void failureAtBeginning() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "D", false));
		todos.add(new Todo(5L, "E", false));
		todos.add(new Todo(6L, "F", false));
		
		JsonPatch patch = readJsonPatch("/hello/patch-failing-operation-first.json");

		try {
			List<Todo> patchedTodos = (List<Todo>) patch.apply(todos);
			fail();
		} catch (JsonPatchException e) {
			assertEquals("Test against path '/5/description' failed", e.getMessage());
		}
		
		// nothing should have changed
		assertEquals(6, todos.size());
		assertFalse(todos.get(1).isComplete());
		assertEquals("D", todos.get(3).getDescription());
		assertEquals("E", todos.get(4).getDescription());
		assertEquals("F", todos.get(5).getDescription());
	}
	
	@Test
	public void failureInMiddle() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "D", false));
		todos.add(new Todo(5L, "E", false));
		todos.add(new Todo(6L, "F", false));
		
		JsonPatch patch = readJsonPatch("/hello/patch-failing-operation-in-middle.json");

		try {
			List<Todo> patchedTodos = (List<Todo>) patch.apply(todos);
			fail();
		} catch (JsonPatchException e) {
			assertEquals("Test against path '/5/description' failed", e.getMessage());
		}
		
		// nothing should have changed
		assertEquals(6, todos.size());
		assertFalse(todos.get(1).isComplete());
		assertEquals("D", todos.get(3).getDescription());
		assertEquals("E", todos.get(4).getDescription());
		assertEquals("F", todos.get(5).getDescription());
	}

	
	
	private JsonPatch readJsonPatch(String jsonPatchFile) throws IOException, JsonParseException, JsonMappingException {
		ClassPathResource resource = new ClassPathResource(jsonPatchFile);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readValue(resource.getInputStream(), JsonNode.class);
		JsonPatch patch = JsonPatch.fromJsonNode((JsonNode) node);
		return patch;
	}
	
}
