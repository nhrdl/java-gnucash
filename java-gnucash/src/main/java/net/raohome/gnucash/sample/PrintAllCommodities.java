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
package net.raohome.gnucash.sample;

import java.util.ArrayList;
import java.util.List;

import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.CommodityTable;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class PrintAllCommodities {

	private static record CommodityRecord(String namespace, Commodity commodity) {}

		
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage AccountTotals <gnucash file path>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();

			CommodityTable commidityTable = session.getBook().getCommidityTable();
			commidityTable.getCommoditiesForNamespace("nonexist");
			
			List<String> namespaces = commidityTable.getNamespaces();
			System.out.printf("Found %d namespaces%n", namespaces.size());
			List<CommodityRecord> list = new ArrayList<>();
			for(String namespace : namespaces) {
				List<Commodity> commodities = commidityTable.getCommoditiesForNamespace(namespace);
				for (Commodity commodity : commodities) {
					list.add(new CommodityRecord(namespace, commodity));
				}
			}
			
			list.sort((a,b)-> {
				int val = a.namespace.compareTo(b.namespace);
				if (val != 0) {
					return val;
				}
				return a.commodity.getFullName().compareTo(b.commodity.getFullName());
			});
			
			for (CommodityRecord rec : list) {
				System.out.printf("%s,%s,%s%n", rec.namespace, rec.commodity.getMnemonic(), rec.commodity.getFullName());
			}
		}
	}
}
