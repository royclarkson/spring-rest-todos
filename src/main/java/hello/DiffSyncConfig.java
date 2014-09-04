package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.config.DiffSyncConfigurerAdapter;
import org.springframework.web.patch.diffsync.config.EnableDifferentialSynchronization;

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
