package net.raohome.gnucash.sample;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Iterator;
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
			NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
			for (String line : lines) {
				String[] data = line.split(",", -1);
				if (data.length != 3) {
					System.out.println(line);
				}
				Commodity commodity = commidityTable.lookup(data[0], data[2]);
				if (commodity != null) {
					BigDecimal latestPrice = priceDB.getLatestPrice(commodity, usd);
					GList<Price> prices = priceDB.getPrice(commodity, usd);

					Iterator<Price> iterator = prices.iterator();
					if (iterator.hasNext()) {
						Price price = iterator.next();
						System.out.printf("%s,%s,%s,%s%n", data[1], currencyInstance.format( price.getValue()), price.getTime(), currencyInstance.format(latestPrice));
					}
					else {
						System.out.printf("%s,(No prices available)%n", data[1]);
					}
				}
			}
		}
	}
}
