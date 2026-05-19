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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

import net.raohome.gnucash.gen.GNUCashBinding;
import net.raohome.gnucash.gen.gnc_numeric;

public abstract class BaseObject {

	protected MemorySegment pointer;

	private static ConcurrentHashMap<MemorySegment, BaseObject> objectMap = new ConcurrentHashMap<>();

	public BaseObject(MemorySegment pointer) {
		this.pointer = pointer;
		if (pointer == null || MemorySegment.NULL.equals(pointer)) {
			throw new NullPointerException();
		}
		objectMap.put(pointer, this);
	}

	public static BigDecimal convertNumber(MemorySegment balance) {

		BigDecimal numerator = BigDecimal.valueOf(gnc_numeric.num(balance));
		BigDecimal denominator = BigDecimal.valueOf(gnc_numeric.denom(balance));

		BigDecimal number = numerator.divide(denominator, 10, RoundingMode.HALF_UP).stripTrailingZeros();
		return number;
	}

	@SuppressWarnings("unchecked")
	public static <T extends BaseObject> T getObjectFor(MemorySegment ptr) {
		return (T) objectMap.get(ptr);
	}

	public static LocalDateTime getTimestamp(long time64) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(time64), ZoneId.systemDefault());

		return ldt;
	}

	public static MemorySegment convertNumber(String toConvert) {
		return GNUCashBinding.gnc_numeric_from_string(Arena.global(), Arena.ofAuto().allocateFrom(toConvert));
	}

	public GNCGUID getGUID() {
		return new GNCGUID(GNUCashBinding.qof_entity_get_guid(pointer));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseObject thatObject) {
			return pointer.equals(thatObject);
		}
		return super.equals(obj);
	}
}
