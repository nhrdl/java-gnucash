package net.raohome.gnucash.sample;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.CommodityTable;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.Price;
import net.raohome.gnucash.objects.PriceDB;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class UpdatePrices {

	// Expects a simple csv file as parameter. CSV file format is Namespce,Commodity
		// Basically output from PrintAllCommodities

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage AccountTotals <gnucash file path> <commodities to update>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			List<String> lines = Files.readAllLines(Paths.get(args[1]));

			session.beginSession(args[0], SessionMode.SESSION_NORMAL_OPEN);
			session.load();
			CommodityTable commidityTable = session.getBook().getCommidityTable();
			Commodity usd = commidityTable.lookup("ISO4217", "USD");

			PriceDB priceDB = session.getBook().getPriceDB();

			NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
			PriceProvider priceDataProvider = new TwelveDataPriceProvider();

			for (String line : lines) {
				String[] data = line.split(",", -1);
				if (data.length != 3) {
					System.out.println(line);
					continue;
				}

				Commodity commodity = commidityTable.lookup(data[0], data[1]);
				if (commodity == null) {
					System.err.printf("Can not find commodity %s/%s%n", data[0], data[1]);
					continue;
				}

				Price latestPrice = priceDB.lookupLatest(commodity, usd);

				Price clone = latestPrice.clone(session.getBook());
				clone.setTime(LocalDateTime.now());
				BigDecimal newPrice = priceDataProvider.latestPrice(data[1]);
				if (newPrice != null) {
					clone.setPrice(newPrice);
					clone.setSourceString("Finance::Quote");
					priceDB.addPrice(clone);
					System.out.printf("%s/%s %s=>%s%n", data[0], data[1],
							currencyInstance.format(latestPrice.getValue()), currencyInstance.format(newPrice));
				}

			}

			session.save();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
