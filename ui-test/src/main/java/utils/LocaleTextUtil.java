package utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads localized UI strings straight from the signup portal locale files
 * ({portalUrl}/locales/{lang}.json), so text assertions stay in sync with the
 * application copy instead of being hard-coded in tests.
 */
public class LocaleTextUtil {

	private static final Logger logger = Logger.getLogger(LocaleTextUtil.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, JsonNode> cache = new HashMap<>();

	private LocaleTextUtil() {
	}

	/**
	 * @param twoLetterLang locale file name without extension, e.g. "en" / "km"
	 * @param key           translation key. Supports nested keys using dot
	 *                      notation, e.g. "something_went_wrong" or
	 *                      "error_response.invalid_transaction"
	 * @return the translated value for the given key and language
	 */
	public static String get(String twoLetterLang, String key) {
		JsonNode value = load(twoLetterLang);
		for (String part : key.split("\\.")) {
			value = (value == null) ? null : value.get(part);
		}
		if (value == null || value.isMissingNode()) {
			throw new RuntimeException(
					"Locale key '" + key + "' not found in '" + twoLetterLang + "' locale file");
		}
		return value.asText();
	}

	private static synchronized JsonNode load(String lang) {
		if (cache.containsKey(lang)) {
			return cache.get(lang);
		}
		String base = EsignetConfigManager.getSignupPortalUrl();
		if (!base.endsWith("/")) {
			base = base + "/";
		}
		String url = base + "locales/" + lang + ".json";
		int timeoutMs = EsignetConfigManager.getTimeout() * 1000;
		try {
			URLConnection conn = URI.create(url).toURL().openConnection();
			conn.setConnectTimeout(timeoutMs);
			conn.setReadTimeout(timeoutMs);
			try (InputStream in = conn.getInputStream()) {
				String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				JsonNode node = MAPPER.readTree(json);
				cache.put(lang, node);
				return node;
			}
		} catch (IOException e) {
			logger.error("Failed to load locale file: " + url, e);
			throw new RuntimeException("Failed to load locale file: " + url, e);
		}
	}
}
