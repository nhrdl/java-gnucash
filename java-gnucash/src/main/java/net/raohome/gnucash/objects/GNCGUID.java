package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;

import net.raohome.gnucash.gen.GNUCashBinding;

public class GNCGUID {

	private MemorySegment pointer;

	public GNCGUID(MemorySegment pointer) {
		this.pointer = pointer;
		
	}
	
	public String guidToString() {
		return GNUCashBinding.guid_to_string(pointer).getString(0);
	}
}
