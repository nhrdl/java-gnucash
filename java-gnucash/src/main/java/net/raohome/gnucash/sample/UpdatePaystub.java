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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;

import net.raohome.Paystub;
import net.raohome.Paystub.LineItem;
import net.raohome.Paystub.PaystubInformation;
import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.JSONUtils;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Split;
import net.raohome.gnucash.objects.Session.SessionMode;
import net.raohome.gnucash.objects.Transaction;

import static net.raohome.gnucash.sample.UpdateMortgateRecord.findAccountForGUID;

public class UpdatePaystub {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err
					.println("Usage AccountTotals <gnucash file path> <paystub splits defintion path> <pdf file path>");
			System.exit(1);
		}
		Engine.init();
		try (Session session = Session.newSession()) {
			session.beginSession(args[0], SessionMode.SESSION_NORMAL_OPEN);
			session.load();

			TypeReference<Map<String, String>> splitAccountsTableRef = new TypeReference<Map<String, String>>() {
			};

			Map<String, String> splitAccountsTable = JSONUtils.fromJson(Paths.get(args[1]), splitAccountsTableRef);

			GList<Account> list = session.getBook().getRootAccount().getDescendends();

			List<Account> javaList = list.toJavaList(Account::new);

			Collection<PaystubInformation> paystubInformationList = Paystub.getPaystubInformation(args[2]);
			TransactionHandler handler = new TransactionHandler();

			handler.javaList = javaList;
			handler.splitAccountsTable = splitAccountsTable;
			handler.session = session;

			String comment = String.format("Imported from %s", args[2]);

			for (PaystubInformation psi : paystubInformationList) {

				Transaction trans = createTransaction(session, comment, psi.payDate);

				System.out.printf("%s%n", psi);
				handler.transaction = trans;

				handler.addNegativeSplit("Gross", psi.gross);
				handler.addSplit("Net Pay", psi.netPay);

				for (LineItem lineItem : psi.deductions) {
					handler.addSplit(lineItem.key(), lineItem.amount());
				}

				for (LineItem lineItem : psi.taxes) {
					handler.addSplit(lineItem.key(), lineItem.amount());
				}

				trans.commitEdit();
			}
			
			session.save();
		}
	}

	private static Transaction createTransaction(Session session, String comment, LocalDate date) {
		Commodity usd = session.getBook().getCommidityTable().lookup("CURRENCY", "USD");

		Transaction trans = Transaction.newTransaction(session.getBook());
		trans.beginEdit();

		trans.setCurrency(usd);
		trans.setDescription(comment);
		trans.setDate(date);
		return trans;
	}

	static class TransactionHandler {
		public Transaction transaction;
		List<Account> javaList;
		Map<String, String> splitAccountsTable;
		Session session;

		public Split addNegativeSplit(String keyword, BigDecimal amount) {
			return addSplit(keyword, amount.negate());
		}

		public Split createSplit(String keyword) {
			String guid = splitAccountsTable.get(keyword);
			if (guid == null) {
				System.err.printf("Cannot find account for %s%n", keyword);
				return null;
			}
			Optional<Account> account = findAccountForGUID(javaList, guid);
			Split split = Split.newSplit(session.getBook());
			split.setAccount(account.orElseThrow());
			split.setParent(transaction);
			
			return split;
		}
		public Split addShareSplit(String keyword, BigDecimal value, BigDecimal quantity) {
			String guid = splitAccountsTable.get(keyword);
			if (guid == null) {
				System.err.printf("Cannot find account for %s%n", keyword);
				return null;
			}
			Optional<Account> account = findAccountForGUID(javaList, guid);
			Split split = Split.newSplit(session.getBook());
			split.setAccount(account.orElseThrow());
			split.setParent(transaction);
			
			split.setValue(value);
			split.setAmount(quantity);
			
			return split;
		}
		
		public Split addSplit(String keyword, BigDecimal amount) {
			String guid = splitAccountsTable.get(keyword);
			if (guid == null) {
				System.err.printf("Cannot find account for %s%n", keyword);
				return null;
			}
			Optional<Account> account = findAccountForGUID(javaList, guid);
			Split split = Split.newSplit(session.getBook());
			split.setValue(amount);
			split.setAccount(account.orElseThrow());
			split.setAmount(amount);
			split.setMemo(keyword);
			split.setParent(transaction);
			return split;
		}
	}
}
