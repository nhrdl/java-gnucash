package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;
import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Split extends BaseObject {

	public Split(MemorySegment pointer) {
		super(pointer);
	}

	public static Split newSplit(Book book) {
		MemorySegment splitPointer = xaccMallocSplit(book.pointer);
		return new Split(splitPointer);
	}
}
