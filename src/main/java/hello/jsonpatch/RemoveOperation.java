package hello.jsonpatch;

/**
 * <p>JSON Patch "remove" operation.</p>
 * 
 * <p>
 * Removes the value at the given path.
 * Will throw a JsonPatchException if the given path isn't valid or if the path is non-nullable.
 * </p>
 * 
 * @author Craig Walls
 */
public class RemoveOperation extends JsonPatchOperation {

	/**
	 * Constructs the remove operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 */
	public RemoveOperation(String path) {
		super("remove", path);
	}
	
	@Override
	void perform(Object target) {
		popValueAtPath(target, path);
	}

}
