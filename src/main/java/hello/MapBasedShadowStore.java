package hello;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MapBasedShadowStore implements ShadowStore<Object> {

	private Map<String, Object> store = new HashMap<String, Object>();
	
	@Override
	public void putShadow(String resourcePath, Object shadow) {		
		store.put(resourcePath, shadow);
	}

	@Override
	public Object getShadow(String resourcePath) {
		return store.get(resourcePath);
	}

}
