package hello.jsonpatch;


public class ReplaceOperation extends JsonPatchOperation {

	private final Object value;

	public ReplaceOperation(String path, Object value) {
		super("replace", path);
		this.value = value;
	}
	
	@Override
	public void perform(Object targetObject) {
		SpELPath spel = getSpELPath();
		spel.setValue(targetObject, value);
	}
	
}
