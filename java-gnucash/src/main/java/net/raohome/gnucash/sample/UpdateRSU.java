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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import net.raohome.RSU;
import net.raohome.RSU.RSURecord;
import net.raohome.gnucash.objects.Account;
import net.raohome.gnucash.objects.Commodity;
import net.raohome.gnucash.objects.Engine;
import net.raohome.gnucash.objects.GList;
import net.raohome.gnucash.objects.JSONUtils;
import net.raohome.gnucash.objects.Session;
import net.raohome.gnucash.objects.Transaction;
import net.raohome.gnucash.objects.Session.SessionMode;
import net.raohome.gnucash.objects.Split;
import net.raohome.gnucash.sample.UpdatePaystub.TransactionHandler;

public class UpdateRSU {

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

			Commodity usd = session.getBook().getCommidityTable().lookup("CURRENCY", "USD");
			GList<Account> list = session.getBook().getRootAccount().getDescendends();

			List<Account> javaList = list.toJavaList(Account::new);

			List<RSURecord> rsuRecords = RSU.process(args[2]);

			TransactionHandler handler = new TransactionHandler();

			handler.javaList = javaList;
			handler.splitAccountsTable = splitAccountsTable;
			handler.session = session;

			String comment = String.format("Imported from %s", args[2]);

			// To commodity account
			// Value => rsuRecord.value
			// Quantity => awarded
			for (RSURecord rsuRecord : rsuRecords) {
				System.out.println(rsuRecord);

				{

					Transaction trans = createTransaction(session, comment, rsuRecord.date, usd);
					handler.transaction = trans;
					Split split = handler.createSplit("commodity");
					split.setValue(rsuRecord.value);
					split.setAmount(rsuRecord.awarded);

					handler.addShareSplit("RSU Income", rsuRecord.value.negate(), rsuRecord.value);
					trans.commitEdit();
				}

				{
					Transaction trans = createTransaction(session, comment, rsuRecord.date, usd);
					handler.transaction = trans;
					Split split = handler.createSplit("commodity");

					BigDecimal withheldAmt = rsuRecord.withheld.multiply(rsuRecord.releasePrice);
					split.setValue(withheldAmt.negate());
					split.setAmount(rsuRecord.withheld.negate());

					handler.addShareSplit("expenses", withheldAmt, withheldAmt);
					trans.commitEdit();
				}

			}
			session.save();
		}

		try (GZIPInputStream gs = new GZIPInputStream(Files.newInputStream(Paths.get(args[0])));
				OutputStream os = Files.newOutputStream(Paths.get("/tmp/expanded.xml"))) {

			IOUtils.copy(gs, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Transaction createTransaction(Session session, String comment, LocalDate date, Commodity commodity) {

		Transaction trans = Transaction.newTransaction(session.getBook());
		trans.beginEdit();

		trans.setCurrency(commodity);
		trans.setDescription(comment);
		trans.setDate(date);
		return trans;
	}
}
