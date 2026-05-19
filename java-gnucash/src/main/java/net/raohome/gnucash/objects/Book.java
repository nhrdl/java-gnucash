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

import java.lang.foreign.MemorySegment;


public class Book extends BaseObject {

	private Book(MemorySegment pointer) {
		super(pointer);
	}

	public static Book newBook() {
		MemorySegment segment = qof_book_new();
		return new Book(segment);
	}
	
	public Account getRootAccount() {
		MemorySegment accountPtr = gnc_book_get_root_account(pointer);
		return new Account(accountPtr);
	}
	
	public CommodityTable getCommidityTable() {
		MemorySegment segment = gnc_commodity_table_get_table(pointer);
		return new CommodityTable(segment);
	}
	
	public PriceDB getPriceDB() {
		MemorySegment segment = gnc_pricedb_get_db(pointer);
		return new PriceDB(segment);
	}
}
