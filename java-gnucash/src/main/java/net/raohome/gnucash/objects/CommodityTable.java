/********************************************************************\
 * This program is free software; you can redistribute it and/or    *
 * modify it under the terms of the GNU General Public License as   *
 * published by the Free Software Foundation; either version 2 of   *
 * the License, or (at your option) any later version.              *
 *                                                                  *
 * This program is distributed in the hope that it will be useful,  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    *
 * GNU General Public License for more details.                     *
 *                                                                  *
 * You should have received a copy of the GNU General Public License*
 * along with this program; if not, contact:                        *
 *                                                                  *
 * Free Software Foundation           Voice:  +1-617-542-5942       *
 * 51 Franklin Street, Fifth Floor    Fax:    +1-617-542-2652       *
 * Boston, MA  02110-1301,  USA       gnu@gnu.org                   *
 *                                                                  *
\********************************************************************/
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
		if (commodity.equals(MemorySegment.NULL)) {
			return null;
		}
		return new Commodity(commodity);
	}

	public Commodity findFull(String namespace, String fullname) {
		MemorySegment namespacePtr = Arena.ofAuto().allocateFrom(namespace);
		MemorySegment fullnamePtr = Arena.ofAuto().allocateFrom(fullname);

		MemorySegment commodity = gnc_commodity_table_find_full(pointer, namespacePtr, fullnamePtr);
		if (commodity.equals(MemorySegment.NULL)) {
			return null;
		}
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
