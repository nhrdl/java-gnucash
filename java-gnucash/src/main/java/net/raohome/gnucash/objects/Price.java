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
