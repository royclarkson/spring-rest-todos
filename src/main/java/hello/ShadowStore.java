package hello;

public interface ShadowStore<T> {
	
	void putShadow(String resourcePath, T shadow);
	
	T getShadow(String resourcePath);
}
