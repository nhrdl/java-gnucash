package net.raohome.gnucash.sample;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import net.raohome.Mortgage;
import net.raohome.PaymentRecord;
import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.JSONUtils;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;
import net.raohome.gnucash.objects.Split;
import net.raohome.gnucash.objects.Transaction;

public class UpdateMortgateRecord {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println(
					"Usage AccountTotals <gnucash file path> <mortgate splits defintion path> <pdf file path>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_NORMAL_OPEN);
			session.load();

			TypeReference<Map<String, String>> splitAccountsTableRef = new TypeReference<Map<String, String>>() {
			};

			Map<String, String> splitAccountsTable = JSONUtils.fromJson(Paths.get(args[1]), splitAccountsTableRef);

			System.out.printf("Found %d split account entries%n", splitAccountsTable.size());
			GList<Account> list = session.getBook().getRootAccount().getDescendends();

			List<Account> javaList = list.toJavaList(Account::new);

			PaymentRecord mortgageData = Mortgage.getMortgateData(args[2]);

			System.out.printf("%s%n", mortgageData);
			Commodity usd = session.getBook().getCommidityTable().lookup("CURRENCY", "USD");

			Optional<Account> mortgageAccount = findAccountForGUID(javaList,
					splitAccountsTable.get("mortgage account guid"));

			{
				BigDecimal amount = mortgageData.getEscrow();
				if (BigDecimal.ZERO.equals(amount) == false) {
					Transaction trans = Transaction.newTransaction(session.getBook());
					trans.beginEdit();

					trans.setCurrency(usd);
					trans.setDescription("Trans descr");
					trans.setNotes("trans notes");
					trans.setDocsLink("Updated from " + args[2]);
					trans.setDate(mortgageData.getPostingDate());

					String acctGuid = splitAccountsTable.get("escrow account guid");
					Optional<Account> optional = findAccountForGUID(javaList, acctGuid);

					{
						Split split = Split.newSplit(session.getBook());
						split.setValue(amount);
						split.setAccount(optional.orElseThrow());
						split.setParent(trans);
						
						
						split.setMemo("Split memo for escrow");

						split.setAmount(amount);
					}

					{
						Split split = Split.newSplit(session.getBook());
						split.setParent(trans);

						split.setAccount(mortgageAccount.orElseThrow());
						split.setAmount(amount.negate());
						split.setMemo("Split memo for escrow");

						split.setValue(amount.negate());
					}

					trans.commitEdit();

				}
			}

			session.save();
		}

	}

	public static Optional<Account> findAccountForGUID(List<Account> list, String guid) {
		return list.stream().filter(t -> {
			return guid.equals(t.getGUID().guidToString());
		}).findFirst();
	}
}
