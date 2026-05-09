package net.raohome.gnucash.sample;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TwelveDataPriceProvider implements PriceProvider {

	private static ObjectMapper mapper;

	static {
		final JsonFactory factory = new JsonFactory();
		mapper = new ObjectMapper(factory);
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, null == System.getProperty("SKIP_STRICT"));
		// convertValue uses valueToTree(), so serialization inclusion applies.
		// NON_EMPTY strips empty
		// collections from that tree (e.g. cookies=[]), then the field reads back as
		// null.
		mapper.setDefaultPropertyInclusion(
				JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
	}

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

		URI url = URI.create(sUrl);
		try (InputStream is = url.toURL().openStream()) {
			// byte[] allBytes = is.readAllBytes();
			// String str = new String(allBytes);
			ObjectReader reader = mapper.readerFor(PriceInformation.class);
			PriceInformation info = reader.readValue(is);
			if (info.code != null) {
				System.out.printf("Error, cannot find price for %s, message %s%n",symbol, info.status);
			}
			return info.price;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		new TwelveDataPriceProvider().latestPrice("PAY");
	}

}
