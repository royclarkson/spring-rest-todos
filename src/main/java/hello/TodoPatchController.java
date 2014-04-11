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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Roy Clarkson
 * @author Craig Walls
 * @author Greg L. Turnquist
 */
@RestController
@RequestMapping("/todos")
public class TodoPatchController extends JsonPatchControllerSupport<Todo, Long> {

	private TodoRepository repository;

	@Autowired
	public TodoPatchController(TodoRepository repository) {
		super(Todo.class);
		this.repository = repository;
	}

	@Override
	protected Todo getEntity(Long id) {
		return repository.findOne(id);
	}

	@Override
	protected Todo saveEntity(Todo todo) {
		Todo saved = repository.save(todo);
		return saved;
	}

	@Override
	protected Iterable<Todo> getEntityList() {
		return repository.findAll();
	}

	@Override
	protected Iterable<Todo> saveEntityList(List<Todo> entityList) {
		Iterable<Todo> saved = repository.save(entityList);
		return saved;
	}

	@Override
	protected void deleteEntity(Todo todo) {
		repository.delete(todo);
	}

}
