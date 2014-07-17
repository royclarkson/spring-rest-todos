package hello.jsonpatch;

import java.io.IOException;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;


public class TestOperation extends JsonPatchOperation {

	private final Object value;

	public TestOperation(String path, Object value) {
		super("test", path);
		this.value = value;
	}
	
	@Override
	public void perform(Object targetObject) {
		Object targetValue = getSpELPath().getValue(targetObject);
		
		try {
			// TODO: This conversion will prove useful in other operations, so it should probably be made part of the parent type
			ObjectMapper mapper = new ObjectMapper();
			Object comparisonValue = mapper.readValue(value.toString(), targetValue.getClass());
			if (!ObjectUtils.nullSafeEquals(comparisonValue, targetValue)) {
				throw new JsonPatchException("Test against path '" + path + "' failed");
			}
		} catch (IOException e) {
			throw new JsonPatchException("Test against path '" + path + "' failed");
		}
		
		
	}
	
}
