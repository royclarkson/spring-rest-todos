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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.patch.diffsync.MapBasedShadowStore;
import org.springframework.web.patch.diffsync.PersistenceCallback;
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.ShadowStore;

@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		TodoRepository repository = ctx.getBean(TodoRepository.class);
		repository.save(new Todo(1L, "a", false));
		repository.save(new Todo(2L, "b", false));
		repository.save(new Todo(3L, "c", false));
	}

	@Bean
	@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public ShadowStore shadowStore() {
		return new MapBasedShadowStore();
	}
	
	@Bean
	public PersistenceCallback<Todo> todoPersistenceCallback(CrudRepository<Todo, Long> repo) {
		return new JpaPersistenceCallback<Todo>(repo, Todo.class);
	}
	
	@Bean
	public PersistenceCallbackRegistry callbackRegistry(CrudRepository<Todo, Long> repo) {
		PersistenceCallback<Todo> jpaCallback = new JpaPersistenceCallback<Todo>(repo, Todo.class);
		List<PersistenceCallback<?>> callbacks = new ArrayList<PersistenceCallback<?>>();
		callbacks.add(jpaCallback);
		return new PersistenceCallbackRegistry(callbacks);
	}
	
}
