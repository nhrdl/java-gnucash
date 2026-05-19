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
