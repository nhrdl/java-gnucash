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

public class PriceDB extends BaseObject {

	public PriceDB(MemorySegment pointer) {
		super(pointer);
	}
	
	public GList<Price> getPrices(Commodity commodity, Commodity currency) {
		
		MemorySegment prices = gnc_pricedb_get_prices(pointer, commodity.pointer, currency.pointer);
		if (MemorySegment.NULL.equals(prices)) {
			return null;
		}
		return new GList<Price>(prices, Price::new);
	}

	public Price lookupLatest(Commodity original, Commodity dest) {
		MemorySegment latest = gnc_pricedb_lookup_latest(pointer, original.pointer, dest.pointer);
		return new Price(latest);
	}
	public BigDecimal getLatestPrice(Commodity original, Commodity dest) {
		MemorySegment latest_price = gnc_pricedb_get_latest_price(Arena.ofAuto(), pointer, original.pointer, dest.pointer);
		return convertNumber(latest_price);
	}

	public void addPrice(Price price) {
		gnc_pricedb_add_price(pointer, price.pointer);
		
	}
}
