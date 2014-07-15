package hello;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MapBasedShadowStore implements ShadowStore<JsonNode> {

	private Map<String, JsonNode> store = new HashMap<String, JsonNode>();
	
	@Override
	public void putShadow(String resourcePath, JsonNode shadow) {		
		store.put(resourcePath, shadow);
	}

	@Override
	public JsonNode getShadow(String resourcePath) {
		return store.get(resourcePath);
	}

}
