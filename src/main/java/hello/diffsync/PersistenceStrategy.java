package hello.diffsync;

public interface PersistenceStrategy<T> {

	T find();
	
	T save(T t);
	
	void delete(T t);
	
}
