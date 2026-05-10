package net.raohome.gnucash.sample;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Session.SessionMode;

public class CheckBalancesFromCSV {

	/**
	 * CSV File I get has two types of records. One contains Account
	 * Number,Investment Name,Symbol,Shares,Share Price,Total Value,
	 * 
	 * and other contains Account Number,Trade Date,Settlement Date,Transaction
	 * Type,Transaction Description,Investment Name,Symbol,Shares,Share
	 * Price,Principal Amount,Commissions and Fees,Net Amount,Accrued
	 * Interest,Account Type,
	 * 
	 * We walk through and check if balances are matching. If not, we log the
	 * transactions
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage AccountTotals <gnucash file path> <csv file>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_READ_ONLY);
			session.load();

			List<String> csvEntries = Files.readAllLines(Paths.get(args[1]));

			List<TotalBalance> totalBalances = new ArrayList<>();
			List<Transaction> transactions = new ArrayList<>();
			for (String line : csvEntries) {
				String[] data = line.split(",", 0);
				if (data.length == 6) {
					if (ACCTNO_PATTERN.test(data[0])) {
						TotalBalance balance = new TotalBalance();
						totalBalances.add(balance);

						balance.acctNo = data[0];
						balance.investment = data[1];
						balance.symbol = data[2];
						balance.shares = new BigDecimal(data[3]);
						balance.price = new BigDecimal(data[4]);
						balance.value = new BigDecimal(data[5]);
					}
				} else {
					if (ACCTNO_PATTERN.test(data[0])) {
						Transaction trans = new Transaction();
						trans.accountNumber = data[0];
						trans.tradeDate = data[1];
						trans.settlementDate = data[2];
						trans.transactionType = data[3];
						trans.transactionDescription = data[4];
						trans.investmentName = data[5];
						trans.symbol = data[6];
						trans.shares = new BigDecimal(data[7]);
						trans.sharePrice = new BigDecimal(data[8]).negate();
						trans.principalAmount = new BigDecimal(data[9]).negate();
						trans.commissions = data[10];
						trans.netAmount = data[11];

						transactions.add(trans);
					}
				}
			}

			GList<Account> gnucashAcctList = session.getBook().getRootAccount().getDescendends();
			Map<String, Account> accountsMap = new HashMap<String, Account>();

			for (Account acct : gnucashAcctList) {
				accountsMap.put(acct.getName(), acct);
			}
			Set<String> accountNames = accountsMap.keySet();
			for (TotalBalance total : totalBalances) {
				for (String name : accountNames) {
					if (name.contains(total.acctNo)) {
						System.out.printf("Found account number %s for symbol %s%n", name, total.symbol);
						Account symbolAccount = findSymbolAccount(total, accountsMap.get(name));
						if (symbolAccount == null) {
							System.out.printf("No account found for %s%n", total.symbol);
							continue;
						}
						System.out.printf("Balances :CSV %s ^^^^^ gnucash balance %s%n", symbolAccount.getBalance(), total.shares);
					}
				}
			}
		}
	}

	private static Account findSymbolAccount(TotalBalance total, Account account) {

		for (Account child : account.getDescendends()) {
			Commodity cmdty = child.getCommodity();
			if (cmdty.getMnemonic().equals(total.symbol)) {
				System.out.printf("Found subaccount for symbol %s%n", total.symbol);
				return child;
			}
		}
		return null;
	}

	static final Predicate<String> ACCTNO_PATTERN = Pattern.compile("^[0-9]+$").asMatchPredicate();

	static class TotalBalance {
		public String acctNo, investment, symbol;
		public BigDecimal shares, price, value;
	}

	static class Transaction {
		String accountNumber, tradeDate, settlementDate, transactionType, transactionDescription, investmentName,
				symbol, commissions, netAmount, accruedInterest, accountType;
		BigDecimal shares, sharePrice, principalAmount;

		@Override
		public String toString() {
			return String.format("Account %s, Fund %s, Date :%s, Type %s, Shares %s , Amount %s ", accountNumber,
					symbol, settlementDate, transactionType, shares, principalAmount);
		}
	}
}
