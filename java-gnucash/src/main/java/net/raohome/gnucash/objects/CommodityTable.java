package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

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

	public List<String> getNamespaces() {
		MemorySegment clist = gnc_commodity_table_get_namespaces(pointer);
		try {
			MemorySegment current = clist;
			List<String> javaList = new ArrayList<String>();
			while (current != null && !current.equals(MemorySegment.NULL)) {

				MemorySegment data = net.raohome.gnucash.gen.GList.data(current);
				if (!data.equals(MemorySegment.NULL)) {
					javaList.add(data.getString(0));
				}
				current = net.raohome.gnucash.gen.GList.next(current);
			}

			return javaList;
		} finally {
			g_list_free(clist);
		}
	}

	public List<Commodity> getCommoditiesForNamespace(String namespace) {

		MemorySegment namespacePtr = Arena.ofAuto().allocateFrom(namespace);
		MemorySegment listPtr = gnc_commodity_table_get_commodities(pointer, namespacePtr);
		List<Commodity> list = new ArrayList<>();
		try {
			if (MemorySegment.NULL.equals(listPtr) == false) {
				GList<Commodity> gList = new GList<Commodity>(listPtr, Commodity::new);
				list.addAll(gList.toJavaList(Commodity::new));
			}
		} finally {
			g_list_free(listPtr);
		}
		return list;
	}
}
