package hello.jsonpatch;

/**
 * <p>JSON Patch "replace" operation.</p>
 * 
 * <p>
 * Replaces the value at the given path with a new value.
 * </p>
 * 
 * @author Craig Walls
 */
public class ReplaceOperation extends JsonPatchOperation {

	private final String value;

	/**
	 * Constructs the replace operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param value The "value" property of the operation in the JSON Patch. The String value should contain valid JSON.
	 */
	public ReplaceOperation(String path, String value) {
		super("replace", path);
		this.value = value;
	}
	
	@Override
	void perform(Object targetObject) {
		setValue(targetObject, value);
	}
	
}
