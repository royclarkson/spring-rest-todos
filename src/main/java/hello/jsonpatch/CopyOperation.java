package hello.jsonpatch;

public class CopyOperation extends JsonPatchOperation {

	private String from;

	public CopyOperation(String path, String from) {
		super("copy", path);
		this.from = from;
	}
	
	@Override
	public void perform(Object target) {
		Object fromValue = new SpELPath(from).getValue(target);
		getSpELPath().addValue(target, fromValue);
	}
	
}
