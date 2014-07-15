package hello;

import static java.util.Arrays.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("unchecked")
public class TodoPatchControllerTest {

	private static final MediaType JSON_PATCH = new MediaType("application", "json-patch+json");
	
	@Test
	public void noChangesFromEitherSide() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content("[]")
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());
		
		// neither save nor delete should be called because nothing changed
		verify(todoRepository, never()).delete(any(Iterable.class));
		verify(todoRepository, never()).save(any(Iterable.class));
	}

	@Test
	public void clientSendsSingleStatusChange() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-single-status"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, never()).delete(any(Iterable.class));		
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(2L, "B", true)));
	}

	@Test
	public void clientSendsAStatusChangeAndADescriptionChangeForSameItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-single-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, never()).delete(any(Iterable.class));
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(2L, "BBB", true)));		
	}

	@Test
	public void clientSendsAStatusChangeAndADescriptionChangeForDifferentItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-two-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, never()).delete(any(Iterable.class));
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(1L, "AAA", false), new Todo(2L, "B", true)));		
	}

	@Test
	public void clientAddsAnItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-add-new-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, never()).delete(any(Iterable.class));
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(null, "D", false)));		
	}
	
	@Test
	public void clientRemovesAnItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, times(1)).delete(any(Iterable.class));
		verify(todoRepository, times(1)).delete(asSet(new Todo(2L, "B", false)));		
		verify(todoRepository, never()).save(any(Iterable.class));		
	}

	@Test
	public void clientRemovesTwoItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-remove-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, times(1)).delete(any(Iterable.class));
		verify(todoRepository, times(1)).delete(asSet(new Todo(2L, "B", false), new Todo(3L, "C", false)));		
		verify(todoRepository, never()).save(any(Iterable.class));		
	}


	@Test
	public void clientUpdatesStatusOnOneItemAndRemovesTwoOtherItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-status-and-delete-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, times(1)).delete(any(Iterable.class));
		verify(todoRepository, times(1)).delete(asSet(new Todo(2L, "B", false), new Todo(3L, "C", false)));		
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(1L, "A", true)));		
	}

	@Test
	public void clientRemovesTwoOtherItemsAndUpdatesStatusOnAnother() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-delete-twoitems-and-change-status-on-another"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, times(1)).delete(any(Iterable.class));
		verify(todoRepository, times(1)).delete(asSet(new Todo(1L, "A", false), new Todo(2L, "B", false)));		
		verify(todoRepository, times(1)).save(any(Iterable.class));		
		verify(todoRepository, times(1)).save(asSet(new Todo(3L, "C", true)));		
	}

	@Test
	public void clientChangesItemStatusAndThenRemovesThatSameItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-modify-then-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		verify(todoRepository, times(1)).delete(any(Iterable.class));
		verify(todoRepository, times(1)).delete(asSet(new Todo(2L, "B", false)));		
		verify(todoRepository, never()).save(any(Iterable.class));		
	}

	
	//
	// private helpers
	//

	private HashSet<Todo> asSet(Todo... todos) {
		return new HashSet<Todo>(asList(todos));
	}

	private String resource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/hello/" + name + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		StringBuilder builder = new StringBuilder();
		while(reader.ready()) {
			builder.append(reader.readLine());
		}
		return builder.toString();
	}


	private TodoRepository todoRepository() {
		TodoRepository todoRepository = mock(TodoRepository.class);
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		when(todoRepository.findAll()).thenReturn(todos);
		return todoRepository;
	}



	private MockMvc mockMvc(TodoRepository todoRepository) {
		ShadowStore<JsonNode> shadowStore = new MapBasedShadowStore();
		TodoPatchController controller = new TodoPatchController(todoRepository, shadowStore);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		MockMvc mvc = standaloneSetup(controller)
				.setCustomArgumentResolvers(new JsonPatchMethodArgumentResolver(messageConverters))
				.build();
		return mvc;
	}
	
}
