/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hello;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

public class PatchCollectionTest {

	@Autowired
	private WebApplicationContext context;

	@Mock
	private TodoRepository repository;

	@InjectMocks
	TodoPatchController controller;

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
		expected.add(new Todo(null, "d", false));
		List<Todo> saved = getTodoList(3);
		saved.add(new Todo(4L, "d", false));
		performPatchRequest("patch-list-add-to-end", "response-list-add-to-end", initial, expected, saved, "\"00c05eb5a529b248712fbeaad0ccf2994\"");
	}

	@Test
	public void patchAddNewTodoAtBeginning() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(null, "d", false));
		expected.addAll(getTodoList(3));
		List<Todo> saved = new ArrayList<Todo>();
		saved.add(new Todo(4L, "d", false));
		saved.addAll(getTodoList(3));
		performPatchRequest("patch-list-add-to-beginning", "response-list-add-to-beginning", initial, expected, saved, "\"0acfa4a7106f27bf444360469392e0fac\"");
	}
	
	
	//
	// Operation: "replace"
	@Test
	public void patchOneComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 1);
		performPatchRequest("patch-list-replace-single-item", "response-emptyPatch", initial, expected, expected, "\"0f39e48479e2253330de0e4c5d6f393e9\"");
	}

	@Test
	public void patchAllComplete() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3, 0, 1, 2);
		performPatchRequest("patch-list-replace-all-items", "response-emptyPatch", initial, expected, expected, "\"02607190d7f23f1e9b435a4f2665299bb\"");
	}
	
	@Test
	public void patchOneDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "a", false));
		expected.add(new Todo(2L, "I've changed", false));
		expected.add(new Todo(3L, "c", false));
		performPatchRequest("patch-list-replace-single-item-description", "response-emptyPatch", initial, expected, expected, "\"065c350be6ff14ba6024a42322d745639\"");
	}

	@Test
	public void patchAllDescription() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "I've changed", false));
		expected.add(new Todo(2L, "Me too", false));
		expected.add(new Todo(3L, "Me three", false));
		performPatchRequest("patch-list-replace-all-items-description", "response-emptyPatch", initial, expected, expected, "\"0809f6a9ae65916a112cc5a4f6211d85a\"");
	}

	
	//
	// Operation: "remove"
	//
	@Test
	public void deleteOne() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3);
		expected.remove(0);
		performPatchRequest("patch-list-remove-item", "response-emptyPatch", initial, expected, expected, "\"0bca2dadff5254d909aa72e0d83bed261\"");
	}

	@Test
	public void deleteMany() throws Exception {
		List<Todo> initial = getTodoList(3);
		List<Todo> expected = getTodoList(3);
		expected.remove(2);
		expected.remove(0);
		performPatchRequest("patch-list-remove-items", "response-emptyPatch", initial, expected, expected, "\"051aca9ebade482cb45146213d8a6af39\"");
		
	}

	//
	// Operation: "move"
	//
	// TODO: A move on a collection resource should essentially be a no-op. It should not be a remove-then-add.
	//       Doing a remove-then-add would essentially leave everything except for the ID of the moved item unchanged.
	//       Since the server assigns IDs, the new ID wouldn't even give the resulting list the order that the client desired.
	//

	
	//
	// Operation: "copy"
	//
	// TODO: Copy may be problematic because the new item will have the same ID as the original. Saving an object with the same ID
	//       will update the original, not create a new item. The fix would be to null out the copy's ID. But if the object is to
	//       be opaque, then how will we know which property to null out?
	
	
	
	//
	// Operation: "test"
	//
	
	
	
	//
	// ETag mismatch
	//
	@SuppressWarnings("unchecked")
	@Test
	public void eTagMismatch() throws Exception {
		List<Todo> initial = getTodoList(3);
		when(repository.findAll()).thenReturn(initial);
		mvc.perform(patch("/todos")
				.content(jsonResource("patch-list-replace-single-item"))
				.header("If-Match", "\"0c2218ebd99cc6cb63ff716a470fa8241\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isConflict());
		verify(repository, never()).save(any(initial.getClass()));
	}

	private void performPatchRequest(String patchJson, String responseJson, List<Todo> initial, List<Todo> expected, List<Todo> saved, String expectedETag) throws Exception, IOException {
		when(repository.findAll()).thenReturn(initial);
		when(repository.save(expected)).thenReturn(saved);
		mvc.perform(patch("/todos")
				.content(jsonResource(patchJson))
				.header("If-Match", "\"0c2218ebd99cc6cb63ff716a470fa8242\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", expectedETag))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource(responseJson)));
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
