package hello.jsonpatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonPatch {

	private final List<JsonPatchOperation> operations;

	@JsonCreator
	public JsonPatch(List<JsonPatchOperation> operations) {
		this.operations = operations;
	}
	
	public int size() {
		return operations.size();
	}
	
	public Object apply(Object in) throws JsonPatchException {
		// TODO: Make defensive copy of in before performing operations so that
		//       if any op fails, the original left untouched
		Object work = in; // not really a defensive copy
		
		for (JsonPatchOperation operation : operations) {
			operation.perform(work);
		}

		return work;
	}

	public static JsonPatch fromJsonNode(JsonNode jsonNode) {
		
		if (!(jsonNode instanceof ArrayNode)) {
			throw new IllegalArgumentException("JsonNode must be an instance of ArrayNode");
		}
		
		ArrayNode opNodes = (ArrayNode) jsonNode;
		List<JsonPatchOperation> ops = new ArrayList<JsonPatchOperation>(opNodes.size());
		for(Iterator<JsonNode> elements = opNodes.elements(); elements.hasNext(); ) {
			JsonNode opNode = elements.next();
			
			String opType = opNode.get("op").textValue();
			String path = opNode.get("path").textValue();
			String value = opNode.has("value") ? opNode.get("value").toString() : null;
			String from = opNode.has("from") ? opNode.get("from").textValue() : null;

			if (opType.equals("test")) {
				ops.add(new TestOperation(path, value));
			} else if (opType.equals("replace")) {
				ops.add(new ReplaceOperation(path, value));
			} else if (opType.equals("remove")) {
				ops.add(new RemoveOperation(path));
			} else if (opType.equals("add")) {
				ops.add(new AddOperation(path, value));
			} else if (opType.equals("copy")) {
				ops.add(new CopyOperation(path, from));
			} else if (opType.equals("move")) {
				ops.add(new MoveOperation(path, from));
			} else {
				throw new JsonPatchException("Unrecognized operation type: " + opType);
			}
		}
		
		return new JsonPatch(ops);
	}
	
}
