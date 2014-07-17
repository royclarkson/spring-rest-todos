package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class AddOperationTest {

	
	@Test
	public void addBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		AddOperation add = new AddOperation("/1/complete", true);
		add.perform(todos);
		
		assertTrue(todos.get(1).isComplete());
	}
	
	
	@Test
	public void addItemToList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		Map<String, Object> value = new HashMap<String, Object>();
		value.put("description", "D");
		value.put("complete", true);
		
		AddOperation add = new AddOperation("/1", value);
		add.perform(todos);
		
		assertEquals(4, todos.size());
	}
	
}
