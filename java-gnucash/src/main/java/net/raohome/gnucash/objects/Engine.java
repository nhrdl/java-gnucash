package net.raohome.gnucash.objects;

import java.lang.foreign.MemorySegment;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Engine {

	public Engine() {
	}

	public static void init() {
		gnc_engine_init(0, MemorySegment.NULL);
	}
}
