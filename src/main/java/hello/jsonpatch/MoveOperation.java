package hello.jsonpatch;

public class MoveOperation extends JsonPatchOperation {

	private String from;

	public MoveOperation(String path, String from) {
		super("copy", path);
		this.from = from;
	}
	
	@Override
	public void perform(Object target) {
		SpELPath fromSpELPath = new SpELPath(from);
		Object fromValue = fromSpELPath.getValue(target);
		fromSpELPath.removeValue(target);
		getSpELPath().addValue(target, fromValue);
	}
	
}
