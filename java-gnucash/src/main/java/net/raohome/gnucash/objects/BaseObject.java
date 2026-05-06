package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

import net.raohome.gnucash.gen.gnc_numeric;

public abstract class BaseObject {

	protected MemorySegment pointer;

	private static ConcurrentHashMap<MemorySegment,  BaseObject> objectMap = new ConcurrentHashMap<>();
	public BaseObject(MemorySegment pointer) {
		this.pointer = pointer;
		objectMap.put(pointer, this);
	}

	public BigDecimal convertNumber(MemorySegment balance) {

		BigDecimal numerator = BigDecimal.valueOf(gnc_numeric.num(balance));
		BigDecimal denominator = BigDecimal.valueOf(gnc_numeric.denom(balance));

		BigDecimal number = numerator.divide(denominator, 10, RoundingMode.HALF_UP).stripTrailingZeros();
		return number;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseObject> T getObjectFor(MemorySegment ptr) {
		return (T) objectMap.get(ptr);
	}
}
