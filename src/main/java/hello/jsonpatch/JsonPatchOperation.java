package hello.jsonpatch;

public abstract class JsonPatchOperation {

	protected final String op;
	
	protected final String path;
	
	public JsonPatchOperation(String op, String path) {
		this.op = op;
		this.path = path;
	}
	
	public String getOp() {
		return op;
	}
	
	public String getPath() {
		return path;
	}
	
	public SpELPath getSpELPath() {
		return new SpELPath(path);
	}
	
	public abstract void perform(Object o);
	
}
