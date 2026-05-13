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
		return GNUCashBinding.gnc_numeric_from_string(Arena.ofAuto(), Arena.ofAuto().allocateFrom(toConvert));
	}

	public GNCGUID getGUID() {
		return new GNCGUID(GNUCashBinding.qof_entity_get_guid(pointer));
	}
}
