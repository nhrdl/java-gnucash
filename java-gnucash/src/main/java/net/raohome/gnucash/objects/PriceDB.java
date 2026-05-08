package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;
import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class PriceDB extends BaseObject {

	public PriceDB(MemorySegment pointer) {
		super(pointer);
	}
	
	public GList<Price> getPrice(Commodity commodity, Commodity currency) {
		
		MemorySegment prices = gnc_pricedb_get_prices(pointer, commodity.pointer, currency.pointer);
		return new GList<Price>(prices, Price::new);
	}

}
