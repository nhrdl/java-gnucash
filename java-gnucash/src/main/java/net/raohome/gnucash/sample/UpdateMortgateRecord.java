package net.raohome.gnucash.sample;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.JSONUtils;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class UpdateMortgateRecord {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println(
					"Usage AccountTotals <gnucash file path> <mortgate splits defintion path> <pdf file path>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();

			TypeReference<Map<String, String>> splitAccountsTableRef = new TypeReference<Map<String, String>>() {
			};

			Map<String, String> splitAccountsTable = JSONUtils.fromJson(Paths.get(args[1]), splitAccountsTableRef);
			
			System.out.printf("Found %d split account entries%n", splitAccountsTable.size());
			GList<Account> list = session.getBook().getRootAccount().getDescendends();
			
			Collection<String> values = splitAccountsTable.values();
			
			Set<Account> splitAccounts = list.toJavaList(Account::new).stream().filter(t-> {
				return values.contains(t.getGUID().guidToString());
			}).collect(Collectors.toSet());
			
			System.out.printf("Found %d accounts in the set%n", splitAccounts.size());
		}

	}
}
