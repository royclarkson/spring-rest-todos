package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.sync.diffsync.PersistenceCallbackRegistry;
import org.springframework.sync.diffsync.config.DiffSyncConfigurerAdapter;
import org.springframework.sync.diffsync.config.EnableDifferentialSynchronization;

@Configuration
@EnableDifferentialSynchronization
public class DiffSyncConfig extends DiffSyncConfigurerAdapter {

	@Autowired
	private PagingAndSortingRepository<Todo, Long> repo;
	
	@Override
	public void addPersistenceCallbacks(PersistenceCallbackRegistry registry) {
		registry.addPersistenceCallback(new JpaPersistenceCallback<Todo>(repo, Todo.class));
	}
	
}
