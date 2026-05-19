/********************************************************************\
 * This program is free software; you can redistribute it and/or    *
 * modify it under the terms of the GNU General Public License as   *
 * published by the Free Software Foundation; either version 2 of   *
 * the License, or (at your option) any later version.              *
 *                                                                  *
 * This program is distributed in the hope that it will be useful,  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    *
 * GNU General Public License for more details.                     *
 *                                                                  *
 * You should have received a copy of the GNU General Public License*
 * along with this program; if not, contact:                        *
 *                                                                  *
 * Free Software Foundation           Voice:  +1-617-542-5942       *
 * 51 Franklin Street, Fifth Floor    Fax:    +1-617-542-2652       *
 * Boston, MA  02110-1301,  USA       gnu@gnu.org                   *
 *                                                                  *
\********************************************************************/
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
						Account symbolAccount = findSymbolAccount(total, accountsMap.get(name));
						if (symbolAccount == null) {
							System.out.printf("No account found for %s%n", total.symbol);
							continue;
						}
						if (symbolAccount.getBalance().equals(total.shares) == false) {
							System.out.printf("%s %s: CSV balance: %s, Gnucash balance: %s%n", total.acctNo, total.symbol, total.shares,
									symbolAccount.getBalance());
							
							List<Transaction> transactionsForAFund = findTransactions(total, transactions);
							for (Transaction t : transactionsForAFund) {
								System.out.printf("%s, Total shares %s%n", t, total.shares);
							}
							
							System.out.println("*******************************************************************************");
						}
					}
				}
			}
		}
	}

	private static List<Transaction> findTransactions(TotalBalance key, List<Transaction> transactions) {

		return transactions.stream().filter(a -> {
			// System.out.println(a.transactionType);
			return a.accountNumber.equals(key.acctNo) && a.symbol.equals(key.symbol)
					&& (a.transactionType.startsWith("Reinvestment") || a.transactionType.equals("Buy")
							|| a.transactionType.equals("Sell"));
		}).toList();
	}
	
	private static Account findSymbolAccount(TotalBalance total, Account account) {

		for (Account child : account.getDescendends()) {
			Commodity cmdty = child.getCommodity();
			if (cmdty.getMnemonic().equals(total.symbol)) {
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
