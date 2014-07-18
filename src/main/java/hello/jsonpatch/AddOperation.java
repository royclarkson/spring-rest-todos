package hello.jsonpatch;

/**
 * <p>JSON Patch "add" operation.</p>
 * 
 * <p>
 * Adds a new value to the given "path".
 * Will throw a JsonPatchException if the path is invalid or if the given value 
 * is not assignable to the given path.
 * </p>
 * 
 * @author Craig Walls
 */
public class AddOperation extends JsonPatchOperation {

	private String value;

	/**
	 * Constructs the add operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param value The "value" property of the operation in the JSON Patch. The String value should contain valid JSON.
	 */
	public AddOperation(String path, String value) {
		super("add", path);
		this.value = value;
	}
	
	@Override
	void perform(Object targetObject) {
		addValue(targetObject, value);
	}
	
}
