package hello.jsonpatch;

import hello.Todo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestOperationTest {

	@Test
	public void testPropertyValueEquals() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", true));
		todos.add(new Todo(3L, "C", false));
		
		TestOperation test = new TestOperation("/0/complete", "false");
		test.perform(todos);

		TestOperation test2 = new TestOperation("/1/complete", "true");
		test2.perform(todos);

	}

	@Test(expected=JsonPatchException.class)
	public void testPropertyValueNotEquals() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", true));
		todos.add(new Todo(3L, "C", false));
		
		TestOperation test = new TestOperation("/0/complete", "true");
		test.perform(todos);
	}

	// TODO: Test list elements
	
}
