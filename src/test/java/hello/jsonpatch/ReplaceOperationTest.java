package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReplaceOperationTest {

	@Test
	public void replaceBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		ReplaceOperation replace = new ReplaceOperation("/1/complete", "true");
		replace.perform(todos);
		
		assertTrue(todos.get(1).isComplete());
	}

	@Test
	public void replaceTextPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		ReplaceOperation replace = new ReplaceOperation("/1/description", "\"BBB\"");
		replace.perform(todos);

		assertEquals("BBB", todos.get(1).getDescription());
	}

	@Test
	public void replaceTextPropertyValueWithANumber() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		ReplaceOperation replace = new ReplaceOperation("/1/description", "22");
		replace.perform(todos);

		assertEquals("22", todos.get(1).getDescription());
	}

}
