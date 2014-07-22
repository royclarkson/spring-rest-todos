package hello.diffutils;

import hello.Todo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class DiffTest {

	@Test
	public void testDiff() throws Exception {

		ListBearer bearer1 = new ListBearer(buildTodoList());
		ListBearer bearer2 = new ListBearer(buildTodoList());
//		bearer2.getList().get(1).setDescription("BBBB");
		
		
		List<ListBearer> original = Arrays.asList(bearer1);
		List<ListBearer> revised = Arrays.asList(bearer2);

		Patch patch = DiffUtils.diff(original, revised);
		List<Delta> deltas = patch.getDeltas();
		System.out.println(deltas.size());
		for (Delta delta : deltas) {
			System.out.println(delta.getType());
			Chunk origChunk = delta.getOriginal();
			System.out.println(origChunk);
			Chunk revisedChunk = delta.getRevised();
			System.out.println(revisedChunk);
		}
		
		
	}
	
	private static class ListBearer {
		
		private List<Todo> list;
		
		public ListBearer(List<Todo> list) {
			this.list = list;
		}
		
		public List<Todo> getList() {
			return list;
		}
		
	}

	
	private List<Todo> buildTodoList() {
		List<Todo> original = new ArrayList<Todo>();
		original.add(new Todo(1L, "A", false));
		original.add(new Todo(2L, "B", false));
		original.add(new Todo(3L, "C", false));
		return original;
	}
}
