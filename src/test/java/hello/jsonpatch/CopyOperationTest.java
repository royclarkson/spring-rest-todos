package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class CopyOperationTest {

	@Test
	public void copyBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/1/complete", "/0/complete");
		copy.perform(todos);

		assertTrue(todos.get(1).isComplete());
	}

	@Test
	public void copyStringPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/1/description", "/0/description");
		copy.perform(todos);

		assertEquals("A", todos.get(1).getDescription());
	}

	@Test
	public void copyBooleanPropertyValueIntoStringProperty() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/1/description", "/0/complete");
		copy.perform(todos);

		assertEquals("true", todos.get(1).getDescription());
	}

	@Test
	public void copyListElementToBeginningOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", true));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/0", "/1");
		copy.perform(todos);
		
		assertEquals(4, todos.size());
		assertEquals(2L, todos.get(0).getId().longValue()); // TODO: This could be problematic if you try to save it to a DB because there'll be duplicate IDs
		assertEquals("B", todos.get(0).getDescription());
		assertTrue(todos.get(0).isComplete());
	}

	@Test
	public void copyListElementToMiddleOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/2", "/0");
		copy.perform(todos);
		
		assertEquals(4, todos.size());
		assertEquals(1L, todos.get(2).getId().longValue()); // TODO: This could be problematic if you try to save it to a DB because there'll be duplicate IDs
		assertEquals("A", todos.get(2).getDescription());
		assertTrue(todos.get(2).isComplete());
	}
	
	@Test
	public void copyListElementToEndOfList_usingIndex() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/3", "/0");
		copy.perform(todos);
		
		assertEquals(4, todos.size());
		assertEquals(1L, todos.get(3).getId().longValue()); // TODO: This could be problematic if you try to save it to a DB because there'll be duplicate IDs
		assertEquals("A", todos.get(3).getDescription());
		assertTrue(todos.get(3).isComplete());
	}
	
	@Test
	@Ignore("TODO: IGNORED UNTIL TILDE SUPPORT IS IMPLEMENTED")
	public void copyListElementToEndOfList_usingTilde() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		CopyOperation copy = new CopyOperation("/~", "/0");
		copy.perform(todos);
		
		assertEquals(4, todos.size());
		assertEquals(1L, todos.get(3).getId().longValue()); // TODO: This could be problematic if you try to save it to a DB because there'll be duplicate IDs
		assertEquals("A", todos.get(3).getDescription());
		assertTrue(todos.get(3).isComplete());
	}
}
