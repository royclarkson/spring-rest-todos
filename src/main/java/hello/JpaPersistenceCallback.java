package hello;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.sync.diffsync.PersistenceCallback;

class JpaPersistenceCallback<T> implements PersistenceCallback<T> {
	
	private final PagingAndSortingRepository<T, Long> repo;
	private Class<T> entityType;

	public JpaPersistenceCallback(PagingAndSortingRepository<T, Long> repo, Class<T> entityType) {
		this.repo = repo;
		this.entityType = entityType;
	}
	
	@Override
	public List<T> findAll() {
		return (List<T>) repo.findAll(new Sort("id"));
	}
	
	@Override
	public T findOne(String id) {
		return repo.findOne(Long.valueOf(id));
	}
	
	@Override
	public void persistChange(T itemToSave) {
		repo.save(itemToSave);
	}
	
	@Override
	public void persistChanges(List<T> itemsToSave, List<T> itemsToDelete) {
		repo.save(itemsToSave);
		repo.delete(itemsToDelete);
	}
	
	@Override
	public Class<T> getEntityType() {
		return entityType;
	}
	
}