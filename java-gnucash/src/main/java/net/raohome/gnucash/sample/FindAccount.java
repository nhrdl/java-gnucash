package net.raohome.gnucash.sample;

import java.util.Optional;

import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class FindAccount {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage AccountTotals <gnucash file path> <account name>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();
			GList<Account> list = session.getBook().getRootAccount().getDescendends();
			Optional<Account> account = list.toJavaList(Account::new).stream().filter(a-> {
				return a.getName().equals(args[1]);
			}).findFirst();
			
			System.out.printf("Found account %s%n", account.isPresent());
		}
	}

}
