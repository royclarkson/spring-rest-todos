package hello.jsonpatch;


public class ReplaceOperation extends JsonPatchOperation {

	private final String value;

	public ReplaceOperation(String path, String value) {
		super("replace", path);
		this.value = value;
	}
	
	@Override
	public void perform(Object targetObject) {
		SpELPath spel = getSpELPath();
		spel.setValue(targetObject, value);
	}
	
}
