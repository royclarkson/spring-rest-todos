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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.ShadowStore;
import org.springframework.web.patch.diffsync.web.DiffSyncController;
import org.springframework.web.patch.jsonpatch.JsonPatchMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		argumentResolvers.add(new JsonPatchMethodArgumentResolver(messageConverters));
	}

	@Bean
	public ShallowEtagHeaderFilter etagFilter() {
		return new ShallowEtagHeaderFilter();
	}
	
	@Bean
	public DiffSyncController diffSyncController(PersistenceCallbackRegistry callbackRegistry, ShadowStore shadowStore) {
		return new DiffSyncController(callbackRegistry, shadowStore);
	}

}
