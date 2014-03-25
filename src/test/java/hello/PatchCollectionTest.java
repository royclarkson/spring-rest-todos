package hello;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

public class PatchCollectionTest {

	@Autowired
	private WebApplicationContext context;

	@Mock
	private TodoRepository repository;

	@InjectMocks
	PatchController controller;

	private MockMvc mvc;

	@Before
	public void setUp() {
		ArrayList<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new MappingJackson2HttpMessageConverter());
		MockitoAnnotations.initMocks(this);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		mvc = MockMvcBuilders
				.standaloneSetup(controller)
				.setCustomArgumentResolvers(new JsonPatchMethodArgumentResolver(messageConverters))
				.build();
	}


	//
	// Operation: "add"
	//
	// NOTE: The add operation is expected to add a new item at the index specified in the patch operation path.
	//       This is flawed, however, because the index, which is not necessarily the same thing as the entity's ID,
	//       is determined by the server and whatever sorting scheme is in place when the client fetched the original
	//       resource collection. Moreover, the list may change between the time that the client creates the patch and
	//       the time that the patch is applied, resulting in a different ordering.
	//       In the end, the best that can be accomplished with the "add" operation is that the new item is created,
	//       but there can be no guarantees with regard to its ultimate position in the resource list.
	@Test
	public void patchAddNewTodoAtEnd() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3);
		expected.add(new Todo(4L, "d", false));
		performPatchRequest("patch-add-todo-at-end", initial, expected, "\"00c05eb5a529b248712fbeaad0ccf2994\"");
	}

	@Test
	public void patchAddNewTodoAtBeginning() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(4L, "d", false));
		expected.addAll(getTodoList(3));
		performPatchRequest("patch-add-todo-at-beginning", initial, expected, "\"0acfa4a7106f27bf444360469392e0fac\"");
	}
	
	
	//
	// Operation: "replace"
	//
	// NOTE: The replace operation is expected to replace an item at the index specified in the patch operation path.
	//       This will work fine so long as the resource list doesn't change. However, the list could change between
	//       the time that the client creates the patch and the time the patch is applied. Thus, the item that the
	//       client intends to replace may no longer be at the same index. Consequently, the replace operation may
	//       replace the wrong item.
	@Test
	public void patchOneComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 1);
		performPatchRequest("patch-replace-single-todo-complete", initial, expected, "\"0f39e48479e2253330de0e4c5d6f393e9\"");
	}

	@Test
	public void patchAllComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 0, 1, 2);
		performPatchRequest("patch-replace-all-todo-complete", initial, expected, "\"02607190d7f23f1e9b435a4f2665299bb\"");
	}
	
	@Test
	public void patchOneDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "a", false));
		expected.add(new Todo(2L, "I've changed", false));
		expected.add(new Todo(3L, "c", false));
		performPatchRequest("patch-replace-single-todo-description", initial, expected, "\"065c350be6ff14ba6024a42322d745639\"");
	}

	@Test
	public void patchAllDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "I've changed", false));
		expected.add(new Todo(2L, "Me too", false));
		expected.add(new Todo(3L, "Me three", false));
		performPatchRequest("patch-replace-all-todo-description", initial, expected, "\"0809f6a9ae65916a112cc5a4f6211d85a\"");
	}

	
	//
	// Operation: "remove"
	//
	@Test
	public void deleteOne() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3);
		expected.remove(0);
		
		// This passes, but only because the mock repository is being called as expected.
		// It does not work in reality, though, because the patch only saves the 2 remaining items, not
		performPatchRequest("patch-remove-todo", initial, expected, "\"0bca2dadff5254d909aa72e0d83bed261\"");
	}
	
	
	//
	// Operation: "move"
	//
	
	
	
	//
	// Operation: "copy"
	//
	
	
	
	//
	// Operation: "test"
	//
	
	
	

	// private helpers
	private void performPatchRequest(String patchJson, List<Todo> initial, List<Todo> expected, String expectedETag)
			throws Exception, IOException {
		when(repository.findAll()).thenReturn(initial);
		mvc.perform(patch("/todos")
				.content(jsonResource(patchJson))
				.header("If-Match", "\"0c2218ebd99cc6cb63ff716a470fa8242\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isNoContent())
				.andExpect(header().string("ETag", expectedETag));
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
