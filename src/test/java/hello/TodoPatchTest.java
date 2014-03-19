package hello;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

//@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
//@ContextConfiguration(classes = Application.class)
public class TodoPatchTest {

	@Autowired
	private WebApplicationContext context;

	@Mock
	private TodoRepository repository;

	@InjectMocks
	MainController mainController;

	private MockMvc mvc;

	@Before
	public void setUp() {
		ArrayList<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new MappingJackson2HttpMessageConverter());
		MockitoAnnotations.initMocks(this);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		mvc = MockMvcBuilders
				.standaloneSetup(mainController)
				.setCustomArgumentResolvers(new JsonPatchMethodArgumentResolver(messageConverters))
				.build();
	}

	@Test
	public void patchOneComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 1);
		performPatchRequest("patch-single-todo-complete", initial, expected);
	}

	@Test
	public void patchAllComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 0, 1, 2);
		performPatchRequest("patch-all-todo-complete", initial, expected);
	}
	
	@Test
	public void patchOneDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "a", false));
		expected.add(new Todo(2L, "I've changed", false));
		expected.add(new Todo(3L, "c", false));
		performPatchRequest("patch-single-todo-description", initial, expected);
	}

	@Test
	public void patchAllDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "I've changed", false));
		expected.add(new Todo(2L, "Me too", false));
		expected.add(new Todo(3L, "Me three", false));
		performPatchRequest("patch-all-todo-description", initial, expected);
	}
	
	@Test
	public void patchAddNewTodoAtEnd() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3);
		expected.add(new Todo(4L, "d", false));
		performPatchRequest("patch-add-todo-at-end", initial, expected);
	}

	// NOTE: The following example is kinda screwy. It saves the patched list with the new member at the beginning, shifting
	//       all other members to the right. That part is how JSON Patch is supposed to work. In the database, however, the
	//       database itself is in control of the ordering of things, so this insert-and-shift will have no bearing on how
	//       things are kept in the database.
	@Test
	public void patchAddNewTodoAtBeginning() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(4L, "d", false));
		expected.addAll(getTodoList(3));
		performPatchRequest("patch-add-todo-at-beginning", initial, expected);
	}


	// private helpers
	private void performPatchRequest(String patchJson, List<Todo> initial, List<Todo> expected)
			throws Exception, IOException {
		when(repository.findAll()).thenReturn(initial);
		
		mvc.perform(patch("/todos")
				.content(jsonResource(patchJson))
				.contentType(MediaType.APPLICATION_JSON));
		verify(repository).save(expected);
	}
	
	private List<Todo> getTodoList(int count, int... trueIndexes) {
		int numberToMake = Math.min(count, 26);
		List<Todo> todoList = new ArrayList<Todo>(count);
		
		List<Integer> trues = new ArrayList<Integer>(trueIndexes.length);
		for (Integer t : trueIndexes) {
			trues.add(t);
		}
		
		char c='a';
		for(int i=0; i<numberToMake; c++, i++) {
			todoList.add(new Todo((long) i+1, "" + c, trues.contains(i)));
		}
		return todoList;
	}
	
	private String jsonResource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/hello/" + name + ".json");
		return StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
	}

}
