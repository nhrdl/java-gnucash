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
package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.raohome.gnucash.gen.TransactionCallback;


public class Account extends BaseObject {

	public Account(MemorySegment pointer) {
		super(pointer);
	}

	public String getDescription() {
		MemorySegment description = xaccAccountGetDescription(pointer);
		return description.getString(0);
	}

	public String getCode() {
		return xaccAccountGetCode(pointer).getString(0);
	}

	public BigDecimal getBalanceInCurrency(Commodity currency) {

		MemorySegment balance = xaccAccountGetBalanceInCurrency(Arena.ofAuto(), pointer, currency.pointer, 1);
		return convertNumber(balance);
	}

	public BigDecimal getBalance() {
		MemorySegment balance = xaccAccountGetBalance(Arena.ofAuto(), pointer);
		return convertNumber(balance);
	}

	public String getName() {
		return xaccAccountGetName(pointer).getString(0);
	}

	public void setDescription(String description) {
		xaccAccountSetDescription(pointer, Arena.ofAuto().allocateFrom(description));
	}

	public GList<Account> getChildrenSorted() {
		var result = gnc_account_get_children_sorted(pointer);

		return new GList<Account>(result, Account::new);
	}

	public List<Account> convertGList(MemorySegment listHead) {
		List<Account> result = new ArrayList<>();

		MemorySegment current = listHead;

		while (current != null && !current.equals(MemorySegment.NULL)) {

			MemorySegment data = net.raohome.gnucash.gen.GList.data(current);
			if (!data.equals(MemorySegment.NULL)) {
				result.add(new Account(data));
			}

			current = net.raohome.gnucash.gen.GList.next(current);
		}

		return result;
	}

	public GList<Account> getDescendends() {
		var result = gnc_account_get_descendants(pointer);
		return new GList<>(result, Account::new);
	}

	public void forEachTransaction(TransactionCallback.Function callback, MemorySegment data) {
		
		MemorySegment callbackPtr = TransactionCallback.allocate(callback, Arena.ofAuto());
		xaccAccountTreeForEachTransaction(pointer, callbackPtr, pointer);
	}
	
	public Commodity getCommodity() {
		return new Commodity(xaccAccountGetCommodity(pointer));
	}
	
	public String getFullName() {
		MemorySegment full_name = gnc_account_get_full_name(this.pointer);
		String name = full_name.getString(0);
		return name;
	}
}
