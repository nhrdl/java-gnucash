package net.raohome.gnucash.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtils {
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

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static <T> T fromJson(Path path, TypeReference<T> ref) {
		try (InputStream is = Files.newInputStream(path)) {
			return fromJson(is, ref);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJson(InputStream is, TypeReference<T> ref) {
		try {
			return mapper.readValue(is, ref);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
