package hello.jsonpatch;

import hello.Todo;

import java.util.Arrays;
import java.util.List;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: After further thought, most/all of this class should be pushed into the JsonPatchOperation class.
public class SpELPath {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	
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
	
	public void setValue(Object target, String valueJson) {
		try {
			Object currentValue = expression.getValue(target);
			Object value = MAPPER.readValue(valueJson, currentValue.getClass());
			
			expression.setValue(target, value);
		} catch (SpelEvaluationException e) {
			throw new JsonPatchException("Unable to set path '" + path + "' to value " + valueJson);
		} catch (Exception e) {
			throw new JsonPatchException("Unable to set path '" + path + "' to value " + valueJson);
		}
	}
	
	public void addValue(Object target, Object value) {
		Object parent = parentExpression != null ? parentExpression.getValue(target) : null;
		if (parent == null || !(parent instanceof List) || listIndex == null) {
			expression.setValue(target, value);
		} else {
			
			try {
				List<Object> list = (List<Object>) parentExpression.getValue(target);
				if (value instanceof String) {
					// BIG HACK HERE!!!
					Todo newTodo = MAPPER.readValue((String) value, Todo.class);
					list.add(listIndex, newTodo);
					// BIG HACK ENDS!!!
				} else {
					list.add(listIndex, value);
				}
			} catch (Exception e) {
				// TODO: HANDLE THIS BETTER!!!
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
