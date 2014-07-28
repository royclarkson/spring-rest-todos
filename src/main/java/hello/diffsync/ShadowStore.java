package hello.diffsync;

public interface ShadowStore {
	
	void putShadow(String resourcePath, Object shadow);
	
	Object getShadow(String resourcePath);

}
