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

// Reads localized strings from the portal's own locale files, so assertions stay in sync with the app copy
public class LocaleTextUtil {

	private static final Logger logger = Logger.getLogger(LocaleTextUtil.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, JsonNode> cache = new HashMap<>();

	private LocaleTextUtil() {
	}

	// Nested keys use dot notation, e.g. "error_response.invalid_transaction"
	public static String get(String twoLetterLang, String key) {
		JsonNode value = load(twoLetterLang);
		for (String part : key.split("\\.")) {
			value = (value == null) ? null : value.get(part);
		}
		if (value == null || value.isMissingNode()) {
			throw new RuntimeException(
					"Locale key '" + key + "' not found in '" + twoLetterLang + "' locale file");
		}
		if (!value.isTextual()) {
			throw new RuntimeException("Locale key '" + key + "' in '" + twoLetterLang
					+ "' locale file is not a text value but " + value.getNodeType()
					+ ", so it holds no translation to assert against");
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
