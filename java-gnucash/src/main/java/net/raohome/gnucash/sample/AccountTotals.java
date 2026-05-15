package net.raohome.gnucash.sample;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class AccountTotals {

	public static void main(String[] args) throws Exception{
		if (args.length != 1) {
			System.err.println("Usage AccountTotals <gnucash file path>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();
			
			GList<Account> list = session.getBook().getRootAccount().getDescendends();

			Commodity usd = session.getBook().getCommidityTable().lookup("ISO4217", "USD");
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
			
			for (Account a : list) {
				if (false == BigDecimal.ZERO.equals(a.getBalanceInCurrency(usd))) {
					System.out.printf("%s\t%s\t%s\t%s\t%s%n", a.getName(), a.getCode(), a.getDescription(),
							nf.format(a.getBalance()), nf.format(a.getBalanceInCurrency(usd)));
				}
			}
		}
	}
}
