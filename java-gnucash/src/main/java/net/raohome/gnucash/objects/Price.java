package net.raohome.gnucash.objects;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

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
}
