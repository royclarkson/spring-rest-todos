package hello.jsonpatch;

import java.io.IOException;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>JSON Patch "test" operation.</p>
 * 
 * <p>
 * If the value given matches the value given at the path, the operation completes as a no-op.
 * On the other hand, if the values do not match or if there are any errors interpreting the path,
 * a JsonPatchException will be thrown.
 * </p>
 * 
 * @author Craig Walls
 */
public class TestOperation extends JsonPatchOperation {

	private final String value;

	/**
	 * Constructs the test operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param value The "value" property of the operation in the JSON Patch. The String value should contain valid JSON.
	 */
	public TestOperation(String path, String value) {
		super("test", path);
		this.value = value;
	}
	
	@Override
	void perform(Object targetObject) {
		Object targetValue = getValue(targetObject);
		
		// targetValue could be null
		
		try {
			// TODO: This conversion will prove useful in other operations, so it should probably be made part of the parent type
			ObjectMapper mapper = new ObjectMapper();
			
			Class<?> targetType = targetValue != null ? targetValue.getClass() : Object.class;
			Object comparisonValue = value != null ? mapper.readValue(value.toString(), targetType) : null;
			if (!ObjectUtils.nullSafeEquals(comparisonValue, targetValue)) {
				throw new JsonPatchException("Test against path '" + path + "' failed");
			}
		} catch (IOException e) {
			throw new JsonPatchException("Test against path '" + path + "' failed.");
		}
	}
	
}
