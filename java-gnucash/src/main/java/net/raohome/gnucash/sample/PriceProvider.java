package net.raohome.gnucash.sample;

import java.math.BigDecimal;

public interface PriceProvider {

	BigDecimal latestPrice(String symbol);
}
