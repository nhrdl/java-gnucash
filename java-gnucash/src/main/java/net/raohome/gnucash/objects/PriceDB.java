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
