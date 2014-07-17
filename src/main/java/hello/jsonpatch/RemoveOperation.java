package hello.jsonpatch;



public class RemoveOperation extends JsonPatchOperation {

	public RemoveOperation(String path) {
		super("remove", path);
	}
	
	@Override
	public void perform(Object targetObject) {
		SpELPath spel = getSpELPath();
		spel.removeValue(targetObject);
	}

}
