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

import static net.raohome.gnucash.gen.GNUCashBinding.gnc_price_clone;
import static net.raohome.gnucash.gen.GNUCashBinding.gnc_price_get_time64;
import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Price extends BaseObject {

	public Price(MemorySegment pointer) {
		super(pointer);
	}

	public BigDecimal getValue() {
		MemorySegment value = gnc_price_get_value(Arena.ofAuto(), pointer);
		if (MemorySegment.NULL.equals(value)) {
			return null;
		}
		return convertNumber(value);
	}
	
	public LocalDateTime getTime() {
		long value = gnc_price_get_time64(pointer);
		return getTimestamp(value);
	}
	
	public Price clone(Book book) {
		MemorySegment value = gnc_price_clone(this.pointer, book.pointer);
		return new Price(value);
	}
	
	public void setTime(LocalDateTime time) {
		gnc_price_set_time64(pointer, time.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(time)));
	}

	public void setPrice(BigDecimal newPrice) {
		MemorySegment number = convertNumber(newPrice.toString());
		gnc_price_set_value(pointer, number);
		
	}

	public void setSourceString(String string) {
		gnc_price_set_source_string(pointer, Arena.ofAuto().allocateFrom(string));
	}
}
