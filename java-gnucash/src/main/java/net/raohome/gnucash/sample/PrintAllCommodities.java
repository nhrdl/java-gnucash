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
				System.out.printf("%s: %s%n", rec.namespace, rec.commodity.getFullName());
			}
		}
	}
}
