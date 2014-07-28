package hello.diffsync;

import hello.jsonpatch.JsonDiff;
import hello.jsonpatch.JsonPatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class DiffSync<T> {

	private JsonPatch patch;
	
	private ShadowStore shadowStore;
	
	private Class<T> entityType;
	
	private Sameness sameness = new IdPropertySameness();

	private PersistenceStrategy<List<T>> persistence;
	
	public DiffSync(JsonPatch patch, ShadowStore shadowStore, PersistenceStrategy<List<T>> persistence, Class<T> entityType) {
		this.patch = patch;
		this.shadowStore = shadowStore;
		this.persistence = persistence;
		this.entityType = entityType;
	}
	
	public void setSameness(Sameness sameness) {
		this.sameness = sameness;
	}
	
	public JsonNode apply() {
		List<T> original = find();
		
		String shadowStoreKey = getShadowStoreKey(original);
		
		List<T> source = deepCloneList(original);
		List<T> shadow = (List<T>) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = deepCloneList(original);
		}

		if (patch.size() > 0) {
			shadow = (List<T>) patch.apply(shadow);
			source = (List<T>) patch.apply(source);

			List<T> itemsToSave = new ArrayList<T>(source);
			itemsToSave.removeAll(original);

			if (itemsToSave.size() > 0) {
				save(itemsToSave);
			}
	
			// REMOVE ITEMS
			List<T> itemsToRemove = new ArrayList<T>(original);
			for (T candidate : original) {
				for (T item : source) {
					if (isSame(candidate, item)) {
						itemsToRemove.remove(candidate);
						break;
					}
				}
			}
			
			if (itemsToRemove.size() > 0) {
				delete(itemsToRemove);
			}
		}
		
		JsonNode returnPatch = new JsonDiff().diff(shadow, source);
		
		// apply return patch to shadow
		shadow = (List<T>) JsonPatch.fromJsonNode(returnPatch).apply(shadow);
		
		// update session with new shadow
		shadowStore.putShadow(shadowStoreKey, shadow);
		
		return returnPatch;
	}
	
	
	// private helper methods
	
	private String getShadowStoreKey(Object o) {
		if (o instanceof List) {
			return "shadow/list/" + entityType.getSimpleName();
		} else {
			return "shadow/" + entityType.getSimpleName();
		}
	}
	
	private boolean isSame(T o1, T o2) {
		return sameness.isSame(o1, o2);
	}
	
	private List<T> find() {
		return persistence.find();
	}
	
	private void save(List<T> list) {
		if (list.size() > 0) {
			persistence.save(list);
		}
	}
	
	private void delete(List<T> list) {
		if (list.size() > 0) {
			persistence.delete(list);
		}
	}

	private List<T> deepCloneList(List<T> original) {
		List<T> copy = new ArrayList<T>(original.size());
		for(T t : original) {
			// TODO : Hokeyness in the following line should be addressed
			copy.add((T) SerializationUtils.clone((Serializable) t)); 
		}
		return copy;
	}

}
