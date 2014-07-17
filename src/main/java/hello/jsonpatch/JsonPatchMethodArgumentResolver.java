package hello.jsonpatch;

import java.util.List;

import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPatchMethodArgumentResolver extends AbstractMessageConverterMethodProcessor {

	public JsonPatchMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	public JsonPatchMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters,
			ContentNegotiationManager contentNegotiationManager) {

		super(messageConverters, contentNegotiationManager);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		try {
			Class.forName("hello.jsonpatch.JsonPatch");
			Class<?> paramType = parameter.getParameterType();
			return JsonPatch.class.isAssignableFrom(paramType);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		String name = Conventions.getVariableNameForParameter(parameter);
		Object argument = readWithMessageConverters(webRequest, parameter, JsonNode.class);

		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, argument, name);
			argument = binder.convertIfNecessary(argument, JsonNode.class, parameter);
		}
		
		JsonNode jsonNode = (JsonNode) argument;
		return JsonPatch.fromJsonNode(jsonNode);
	}

	
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return false;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
	}
}
