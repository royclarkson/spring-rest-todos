package hello.jsonpatch;

/**
 * <p>JSON Patch "copy" operation.</p>
 * 
 * <p>
 * Copies a value from the given "from" path to the given "path".
 * Will throw a JsonPatchException if either path is invalid or if the object at the from path 
 * is not assignable to the given path.
 * </p>
 * 
 * <p>
 * NOTE: When dealing with lists, the copy operation may yield undesirable results.
 * If a list is produced from a database query, it's likely that the list contains items with a unique ID.
 * Copying an item in the list will result with a list that has a duplicate object, with the same ID.
 * The best case post-patch scenario is that each copy of the item will be saved, unchanged, to the database
 * and later queries for the list will not include the duplicate.
 * The worst case post-patch scenario is that a following operation changes some properties of the copy,
 * but does not change the ID.
 * When saved, both the original and the copy will be saved, but the last one saved will overwrite the first.
 * Effectively only one copy will survive post-save.
 * </p>
 * 
 * <p>
 * In light of this, it's probably a good idea to perform a "replace" after a "copy" to set the ID property
 * (which may or may not be "id").
 * </p>
 * 
 * @author Craig Walls
 */
public class CopyOperation extends JsonPatchOperation {

	private String from;

	/**
	 * Constructs the copy operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param from The "from" property of the operation in the JSON Patch. Should be a path (e.g., '/foo/bar/5')
	 */
	public CopyOperation(String path, String from) {
		super("copy", path);
		this.from = from;
	}
	
	@Override
	void perform(Object target) {
		addValue(target, pathToExpression(from).getValue(target));
	}
	
}
