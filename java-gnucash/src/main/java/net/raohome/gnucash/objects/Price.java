package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.gnc_price_get_time64;
import static net.raohome.gnucash.gen.GNUCashBinding.gnc_price_get_value;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.time.Instant;
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
}
