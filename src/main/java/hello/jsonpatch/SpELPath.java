package hello.jsonpatch;

import hello.Todo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

// TODO: After further thought, most/all of this class should be pushed into the JsonPatchOperation class.
public class SpELPath {

	private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
	
	private Expression expression;

	private Expression parentExpression = null;
	
	private Integer listIndex = null;

	private String path;

	public SpELPath(String path) {
		this.path = path;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] pathNodes = path.split("\\/");
		String spel = translate(pathNodes);
		expression = SPEL_EXPRESSION_PARSER.parseExpression(spel);
		
		if (pathNodes.length > 0) {
			String[] parentNodes = Arrays.copyOf(pathNodes, pathNodes.length - 1);
			String parentSpel = translate(parentNodes);
			parentExpression = SPEL_EXPRESSION_PARSER.parseExpression(parentSpel);

			try {
				listIndex = Integer.parseInt(pathNodes[pathNodes.length - 1]);
			} catch (NumberFormatException e) {
				// not a number...leave index null
			}
		}
	}
	
	public Object getValue(Object target) {
		return expression.getValue(target);
	}
	
	public void setValue(Object target, Object value) {
		try {
			expression.setValue(target, value);
		} catch (SpelEvaluationException e) {
			throw new JsonPatchException("Unable to set path '" + path + "' to value " + value);
		}
	}
	
	public void addValue(Object target, Object value) {
		Object parent = parentExpression != null ? parentExpression.getValue(target) : null;
		if (parent == null || !(parent instanceof List) || listIndex == null) {
			expression.setValue(target, value);
		} else {
			
			if (value instanceof Map) {
				// TODO: BIG DOMAIN-SPECIFIC HACK FOLLOWS
				//
				// Should NOT explicitly create an instance of Todo and add it to a List<Todo>
				// 
				// SHOULD...
				//  Somehow determine the type of the object
				//    - This is the hard part
				//    - There's no clue in the JSON (nor should there be)
				//    - There are minimal clues in the list itself (if the list is non-empty, fetch an item and check it's type)
				//  Create an instance
				//  Add it to the list
				//
				List<Todo> list = (List<Todo>) parentExpression.getValue(target);
				Map<String, ?> valueMap = (Map<String, ?>) value;
				Todo newTodo = new Todo(null, (String) valueMap.get("description"), (boolean) valueMap.get("complete"));
				list.add(listIndex, newTodo);
				//
				// BIG HACK ENDS
				//
			} else {
				List<Object> list = (List<Object>) parentExpression.getValue(target);
				list.add(listIndex, value);
			}
			
		}
	}
	
	public void removeValue(Object target) {
		if (listIndex == null || parentExpression == null) {
			try {
				expression.setValue(target, null);
			} catch (NullPointerException e) {
				// TODO: Fail silently or loudly? What should happen if a property isn't nullable?
				throw new JsonPatchException("JSON path '" + path + "' is not nullable.");
			}
		} else {
			List<?> list = (List<?>) parentExpression.getValue(target);
			list.remove(listIndex.intValue());
		}
	}
	
	
	// private helpers

	private String translate(String[] pathNodes) {
		StringBuilder spelBuilder = new StringBuilder();
		
		for(int i=0; i < pathNodes.length; i++) {
			String pathNode = pathNodes[i];
			
			if (pathNode.length() == 0) {
				continue;
			}
			try {
				int index = Integer.parseInt(pathNode);
				spelBuilder.append('[').append(index).append(']');
			} catch (NumberFormatException e) {
				if (spelBuilder.length() > 0) {
					spelBuilder.append('.');	
				}
				spelBuilder.append(pathNode);
			}
		}
		
		String spel = spelBuilder.toString();
		if (spel.length() == 0) {
			spel = "#this";
		}
		return spel;
	}

}
