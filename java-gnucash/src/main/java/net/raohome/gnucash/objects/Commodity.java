package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Commodity extends BaseObject {

	public Commodity(MemorySegment pointer) {
		super(pointer);
	}

	public String getFullName() {
		MemorySegment fullname = gnc_commodity_get_fullname(this.pointer);
		return fullname.getString(0);
	}
	
	public String getNamespace() {
		MemorySegment namespace = gnc_commodity_get_namespace(this.pointer);
		return namespace.getString(0);
	}
}
