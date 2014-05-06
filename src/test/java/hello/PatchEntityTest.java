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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

public class PatchEntityTest {

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
				.build();
	}


	//
	// Operation: "add"
	// Expected behavior: If a property is null, set it to the given value. If the property doesn't exist, then fail.
	@Test
	public void addProperty() throws Exception {
		Todo initial = new Todo(1L, null, false);
		Todo expected = new Todo(1L, "Todo 1", false);
		when(repository.findOne(1L)).thenReturn(initial);
		when(repository.save(expected)).thenReturn(expected);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-add-property"))
				.header("If-Match", "\"0e0578f0a06ae7372c5338e685a42070e\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", "\"0c7beca09dc65fffc1ce33ba05acb97a7\""))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource("response-emptyPatch")));
		verify(repository).save(expected);
	}
	
	@Test
	public void addProperty_existingValue() throws Exception {
		Todo initial = new Todo(1L, "Todo 1", false);
		Todo expected = new Todo(1L, "Todo A", false);
		when(repository.findOne(1L)).thenReturn(initial);
		when(repository.save(expected)).thenReturn(expected);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-add-property_existingValue"))
				.header("If-Match", "\"0c7beca09dc65fffc1ce33ba05acb97a7\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", "\"00e169db820e12ba0cf756ac3099c70ff\""))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource("response-emptyPatch")));
		verify(repository).save(expected);
	}
	
	@Test
	public void addProperty_nonexistentProperty() throws Exception {
		Todo initial = new Todo(1L, null, false);
		when(repository.findOne(1L)).thenReturn(initial);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-add-property_nonexistentProperty"))
				.header("If-Match", "\"0e0578f0a06ae7372c5338e685a42070e\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addProperty_incompatibleType() throws Exception {
		Todo initial = new Todo(1L, "Todo 1", false);
		when(repository.findOne(1L)).thenReturn(initial);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-add-property_incompatibleType"))
				.header("If-Match", "\"0c7beca09dc65fffc1ce33ba05acb97a7\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isUnprocessableEntity());
	}

	//
	// Operation: "replace"
	// Expected behavior: Set a property to the given value. If the property doesn't exist, then fail.

	@Test
	public void replaceProperty() throws Exception {
		Todo initial = new Todo(1L, null, false);
		Todo expected = new Todo(1L, "Todo 1", false);
		when(repository.findOne(1L)).thenReturn(initial);
		when(repository.save(expected)).thenReturn(expected);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-replace-property"))
				.header("If-Match", "\"0e0578f0a06ae7372c5338e685a42070e\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", "\"0c7beca09dc65fffc1ce33ba05acb97a7\""))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource("response-emptyPatch")));
		verify(repository).save(expected);
	}
	
	@Test
	public void replaceProperty_existingValue() throws Exception {
		Todo initial = new Todo(1L, "Todo 1", false);
		Todo expected = new Todo(1L, "Todo A", false);
		when(repository.findOne(1L)).thenReturn(initial);
		when(repository.save(expected)).thenReturn(expected);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-replace-property_existingValue"))
				.header("If-Match", "\"0c7beca09dc65fffc1ce33ba05acb97a7\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", "\"00e169db820e12ba0cf756ac3099c70ff\""))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource("response-emptyPatch")));
		verify(repository).save(expected);
	}
	
	@Test
	public void replaceProperty_nonexistentProperty() throws Exception {
		Todo initial = new Todo(1L, null, false);
		when(repository.findOne(1L)).thenReturn(initial);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-replace-property_nonexistentProperty"))
				.header("If-Match", "\"0e0578f0a06ae7372c5338e685a42070e\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void replaceProperty_incompatibleType() throws Exception {
		Todo initial = new Todo(1L, "Todo 1", false);
		when(repository.findOne(1L)).thenReturn(initial);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-replace-property_incompatibleType"))
				.header("If-Match", "\"0c7beca09dc65fffc1ce33ba05acb97a7\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isUnprocessableEntity());
	}

	
	//
	// Operation: "remove"
	// Expected behavior: Set a property to null. If the property doesn't exist, then fail.
	//
	// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO: Currently sends back an "add" to add the property back as a null. Not sure how to deal with this now.
	// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	@Test
	public void removeProperty() throws Exception {
		Todo initial = new Todo(1L, "Todo 1", false);
		Todo expected = new Todo(1L, null, false);
		when(repository.findOne(1L)).thenReturn(initial);
		when(repository.save(expected)).thenReturn(expected);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-remove-property"))
				.header("If-Match", "\"0c7beca09dc65fffc1ce33ba05acb97a7\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isOk())
				.andExpect(header().string("ETag", "\"0e0578f0a06ae7372c5338e685a42070e\""))
				.andExpect(header().string("Content-Type", "application/json-patch+json"))
				.andExpect(MockMvcResultMatchers.content().string(jsonResource("response-entity-remove-property")));
		verify(repository).save(expected);
	}
	
	@Test
	public void removeProperty_nonexistentProperty() throws Exception {
		Todo initial = new Todo(1L, null, false);
		when(repository.findOne(1L)).thenReturn(initial);
		mvc.perform(patch("/todos/1")
				.content(jsonResource("patch-entity-remove-property_nonexistentProperty"))
				.header("If-Match", "\"0e0578f0a06ae7372c5338e685a42070e\"")
				.contentType(new MediaType("application", "json-patch+json")))
				.andExpect(status().isUnprocessableEntity());
	}
	
	//
	// Operation: "move"
	// Expected behavior: Set the value of property A to the value of property B, then set property B to null. If either property doesn't exist or if the values aren't compatible, then fail.
	
	//
	// Operation: "copy"
	// Expected behavior: Set a property 
	
	//
	// Operation: "test"
	//
	
	
	private String jsonResource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/hello/" + name + ".json");
		return StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
	}

}
