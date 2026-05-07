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

public class AccountsForCommodity {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage AccountTotals <gnucash file path> <commodity namespace> <commodity full name>");
			System.exit(1);
		}
		Engine.init();
		
		String namespace = args[1], fullName = args[2];
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();
			
			Commodity usd = session.getBook().getCommidityTable().lookup("ISO4217", "USD");
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
			
			GList<Account> accounts = session.getBook().getRootAccount().getDescendends();
			BigDecimal unitTotal = new BigDecimal(0), usdTotal = new BigDecimal(0);
			for (Account acct : accounts) {
				Commodity commodity = acct.getCommodity();
				if (namespace.equals(commodity.getNamespace()) && 
						fullName.equals(commodity.getFullName()))
				{
					unitTotal = unitTotal.add(acct.getBalanceInCurrency(commodity));
					usdTotal = usdTotal.add(acct.getBalanceInCurrency(usd));
					
					System.out.printf("%s: %s %s%n", acct.getName(), nf.format(acct.getBalanceInCurrency(commodity)), nf.format(acct.getBalanceInCurrency(usd)));
				}
			}
			
			System.out.println("************************************");
			System.out.printf("Total: %s %s", nf.format(unitTotal), nf.format(usdTotal));
		}
	}

}
