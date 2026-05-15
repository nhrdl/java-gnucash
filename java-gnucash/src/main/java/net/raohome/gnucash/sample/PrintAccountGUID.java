package net.raohome.gnucash.sample;

import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class PrintAccountGUID {
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
			list.forEach(acct-> {
				System.out.printf("%s,%s%n", acct.getGUID().guidToString(), acct.getFullName());
			});
		}
	}
}
