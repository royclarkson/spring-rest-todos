package hello.diffsync;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class MapBasedShadowStore implements ShadowStore {

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
