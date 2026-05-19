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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Split extends BaseObject {

	public Split(MemorySegment pointer) {
		super(pointer);
	}

	public static Split newSplit(Book book) {
		MemorySegment splitPointer = xaccMallocSplit(book.pointer);
		return new Split(splitPointer);
	}
	
	public void setAccount(Account account) {
		xaccSplitSetAccount(pointer, account.pointer);
	}
	
	public void setParent(Transaction trans) {
		xaccSplitSetParent(pointer, trans.pointer);
	}
	
	public void setMemo(String memo) {
		xaccSplitSetMemo(pointer, Arena.ofAuto().allocateFrom(memo));
	}
	
	public void setAmount(BigDecimal amount) {
		MemorySegment strAmt = convertNumber(amount.toString());
		xaccSplitSetAmount(pointer, strAmt);
	}
	
	public void setValue(BigDecimal amount) {
		MemorySegment strAmt = convertNumber(amount.toString());
		xaccSplitSetValue(pointer, strAmt);
	}
	
	public void setSharePriceAndAmount(BigDecimal price, BigDecimal amount) {
		MemorySegment pricePtr = convertNumber(price.toString());
		MemorySegment amountPtr = convertNumber(amount.toString());
		
		xaccSplitSetSharePriceAndAmount(pointer, pricePtr, amountPtr);
	}
}
