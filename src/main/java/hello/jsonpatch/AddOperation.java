package hello.jsonpatch;


// TODO: Figure out what is the difference between "add" and "replace" when applied to Java object properties.
//       Applied to JSON, there's certainly a difference.
//       Applied to Java objects, not so much.
//       When dealing with a Java object property, both add and replace essentially mean "set".
//       When dealing with a Java collection, "add" means "insert" while "replace" means "replace"

public class AddOperation extends JsonPatchOperation {

	private Object value;

	public AddOperation(String path, Object value) {
		super("add", path);
		this.value = value;
	}
	
	@Override
	public void perform(Object targetObject) {
		SpELPath spel = getSpELPath();
		spel.addValue(targetObject, value);
	}

	
}
