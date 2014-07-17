package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RemoveOperationTest {

	@Test
	public void removePropertyFromObject() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		new RemoveOperation("/1/description").perform(todos);
		
		assertNull(todos.get(1).getDescription());
	}

	@Test
	public void removeItemFromList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		new RemoveOperation("/1").perform(todos);
		
		assertEquals(2, todos.size());
		assertEquals("A", todos.get(0).getDescription());
		assertEquals("C", todos.get(1).getDescription());
	}

}
