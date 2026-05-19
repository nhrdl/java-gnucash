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
package net.raohome.gnucash.sample;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.raohome.gnucash.objects.JSONUtils;

public class TwelveDataPriceProvider implements PriceProvider {


	static class PriceInformation {
		private BigDecimal price;
		private Integer code;
		private String message, status;

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}
	}

	@Override
	public BigDecimal latestPrice(String symbol) {
		String key = System.getProperty("TWELVE_MONKEY_API_KEY");
		if (key == null) {
			key = System.getenv("TWELVE_MONKEY_API_KEY");
		}
		if (key == null) {
			throw new NullPointerException("No property defined for \"TWELVE_MONKEY_API_KEY\"");
		}
		String sUrl = String.format("https://api.twelvedata.com/price?symbol=%s&apikey=%s", symbol, key);

		PriceInformation info = makeCall(sUrl,symbol, 0);
		if (info != null) {
			return info.price;
		}
		return null;
	}

	PriceInformation makeCall(String sUrl, String symbol, int count) {
		if (count == 3) {
			System.err.println("Too many attempts. Aborting call for " + sUrl);
			return null;
		}
		URI url = URI.create(sUrl);
		try (InputStream is = url.toURL().openStream()) {
			// byte[] allBytes = is.readAllBytes();
			// String str = new String(allBytes);
			ObjectReader reader = JSONUtils.getMapper().readerFor(PriceInformation.class);
			PriceInformation info = reader.readValue(is);
			if (info.code != null) {
				if (429 == info.code) {
					System.out.println("Encountered rate limit. Waiting...");
					Thread.sleep(Duration.ofSeconds(90));
					return makeCall(sUrl, symbol, count + 1);
				}
				else {
					System.out.printf("Error, cannot find price for %s, message %s, code %d%n", symbol, info.message,
							info.code); // The site rate limits
					return null;
				}
			}
			return info;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("TwelveDataPriceProvider <symbols file>\nOne symbol per line");
			System.exit(1);
		}
		for (String symbol : Files.readAllLines(Paths.get(args[0]))) {

			System.out.printf("%s:%s%n", symbol, new TwelveDataPriceProvider().latestPrice(symbol));
		}

	}

}
