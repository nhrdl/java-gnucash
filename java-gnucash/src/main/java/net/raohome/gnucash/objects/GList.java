package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class GList<T extends BaseObject> extends BaseObject implements Iterable<T> {

	private Function<MemorySegment, T> instanceProvider;

	public GList(MemorySegment pointer, Function<MemorySegment, T> instanceProvider) {
		super(pointer);
		this.instanceProvider = instanceProvider;
	}

	public List<T> toJavaList(Function<MemorySegment, T> instanceProvider) {
		List<T> result = new ArrayList<>();

		MemorySegment current = pointer;

		while (current != null && !current.equals(MemorySegment.NULL)) {

			MemorySegment data = net.raohome.gnucash.gen.GList.data(current);
			if (!data.equals(MemorySegment.NULL)) {
				result.add(instanceProvider.apply(data));
			}

			current = net.raohome.gnucash.gen.GList.next(current);
		}

		return result;
	}

	private static class ListIterator<T> implements Iterator<T> {

		private Function<MemorySegment, T> instanceProvider;

		private MemorySegment current;

		ListIterator(MemorySegment pointer, Function<MemorySegment, T> instanceProvider) {
			this.current = pointer;
			this.instanceProvider = instanceProvider;

		}

		@Override
		public boolean hasNext() {

			return current != null && !current.equals(MemorySegment.NULL);
		}

		@Override
		public T next() {
			MemorySegment data = net.raohome.gnucash.gen.GList.data(current);
			current = net.raohome.gnucash.gen.GList.next(current);
			return instanceProvider.apply(data);
		}

	}

	@Override
	public Iterator<T> iterator() {

		return new ListIterator<>(pointer, instanceProvider);
	}

}
