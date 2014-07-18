package hello.jsonpatch;

/**
 * Exception thrown if an error occurs in the course of applying a JSON Patch.
 * 
 * @author Craig Walls
 */
public class JsonPatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JsonPatchException(String message) {
		super(message);
	}

}
