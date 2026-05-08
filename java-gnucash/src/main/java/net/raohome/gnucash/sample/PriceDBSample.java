package net.raohome.gnucash.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.CommodityTable;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.Price;
import net.raohome.gnucash.objects.PriceDB;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class PriceDBSample {

	// Expects a simple csv file as parameter. CSV file format is Namespce,Commodity
	// Basically output from PrintAllCommodities
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage AccountTotals <gnucash file path> <commodities to update>");
			System.exit(1);
		}
		List<String> lines = Files.readAllLines(Paths.get(args[1]));
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();
			CommodityTable commidityTable = session.getBook().getCommidityTable();
			Commodity usd = commidityTable.lookup("ISO4217", "USD");
			
			PriceDB priceDB = session.getBook().getPriceDB();
			for (String line : lines) {
				String[] data = line.split(",", -1);
				if (data.length != 3) {
					System.out.println(line);
				}
				Commodity commodity = commidityTable.lookup(data[0], data[2]);
				if (commodity != null) {
					GList<Price> prices = priceDB.getPrice(commodity, usd);
					Price price = prices.iterator().next();
					System.out.printf("%s,%s%n", data[1], price.getValue());
				}
			}
		}
	}
}
