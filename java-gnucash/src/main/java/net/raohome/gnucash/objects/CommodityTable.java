package net.raohome.gnucash.objects;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class CommodityTable extends BaseObject {

	public CommodityTable(MemorySegment pointer) {
		super(pointer);
	}

	public Commodity lookup(String namespace, String mnemonic) {
		MemorySegment namespacePtr = Arena.ofAuto().allocateFrom(namespace);
		MemorySegment mnemonicPtr = Arena.ofAuto().allocateFrom(mnemonic);
		
		MemorySegment commodity = gnc_commodity_table_lookup(pointer, namespacePtr, mnemonicPtr);
		return new Commodity(commodity);
	}
}
