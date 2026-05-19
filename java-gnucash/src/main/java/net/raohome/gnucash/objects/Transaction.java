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

import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetDescription;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNotes;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNum;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetSplitList;
import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction extends BaseObject {

	public Transaction(MemorySegment pointer) {
		super(pointer);
	}

	public String getDescription() {
		return xaccTransGetDescription(pointer).getString(0);
	}

	public byte getType() {
		return xaccTransGetTxnType(pointer);
	}

	public String getNum() {
		return xaccTransGetNum(pointer).getString(0);
	}

	public String getNotes() {
		MemorySegment notes = xaccTransGetNotes(pointer);
		if (notes.equals(MemorySegment.NULL) == false)
			return notes.getString(0);

		return null;
	}

	public GList<Split> getSplitList() {
		MemorySegment list = xaccTransGetSplitList(pointer);

		return new GList<Split>(list, Split::new);
	}

	public static Transaction newTransaction(Book book) {
		MemorySegment transactionPtr = xaccMallocTransaction(book.pointer);
		return new Transaction(transactionPtr);
	}

	public void setType(byte type) {
		xaccTransSetTxnType(pointer, type);
	}

	public void setNumber(String number) {
		xaccTransSetNum(pointer, Arena.ofAuto().allocateFrom(number));
	}

	public void setDescription(String descr) {
		xaccTransSetDescription(pointer, Arena.ofAuto().allocateFrom(descr));
	}

	public void setDocsLink(String docLink) {
		xaccTransSetDocLink(pointer, Arena.ofAuto().allocateFrom(docLink));
	}

	public void setNotes(String notes) {
		xaccTransSetNotes(pointer, Arena.ofAuto().allocateFrom(notes));
	}

	public void setDate(LocalDate date) {
		xaccTransSetDate(pointer, date.getDayOfMonth(), date.getMonthValue(), date.getYear());
	}

	public void setCurrency(Commodity commodity) {
		xaccTransSetCurrency(pointer, commodity.pointer);
	}

	public void beginEdit() {
		xaccTransBeginEdit(pointer);
	}

	public void commitEdit() {
		xaccTransCommitEdit(pointer);
	}

	public static void createTransaction(Session session, String description, String notes, String docsLink,
			LocalDate date, Account positiveAcct, Account negativeAccount, BigDecimal amount) {
		Commodity usd = session.getBook().getCommidityTable().lookup("CURRENCY", "USD");

		Transaction trans = Transaction.newTransaction(session.getBook());
		trans.beginEdit();

		trans.setCurrency(usd);
		if (description != null)
			trans.setDescription(description);

		if (notes != null)
			trans.setNotes(notes);

		if (docsLink != null)
			trans.setDocsLink(docsLink);

		trans.setDate(date);

		createSplit(session, positiveAcct, amount).setParent(trans);

		createSplit(session, negativeAccount, amount.negate()).setParent(trans);
		
		trans.commitEdit();

	}

	public static Split createSplit(Session session, Account account, BigDecimal amount) {
		Split split = Split.newSplit(session.getBook());
		split.setValue(amount);
		split.setAccount(account);
		split.setAmount(amount);
		return split;
	}
}
