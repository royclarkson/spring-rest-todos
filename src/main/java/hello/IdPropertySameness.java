package hello;

import java.lang.reflect.Field;

import org.springframework.util.ObjectUtils;


public class IdPropertySameness implements Sameness {

	@Override
	public boolean isSame(Object o1, Object o2) {
		try {
			Field idField1 = o1.getClass().getDeclaredField("id");
			idField1.setAccessible(true);
			Object id1 = idField1.get(o1);
			Field idField2 = o2.getClass().getDeclaredField("id");
			idField2.setAccessible(true);
			Object id2 = idField2.get(o2);
			return ObjectUtils.nullSafeEquals(id1, id2);
		} catch (NoSuchFieldException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		}
	}
	
}
