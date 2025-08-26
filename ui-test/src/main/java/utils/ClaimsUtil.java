package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClaimsUtil {

	public static Map<String, List<String>> parseClaimsFromUrl(String url) throws Exception {
		String encodedClaims = extractQueryParam(url, "claims");
		String decodedClaims = URLDecoder.decode(encodedClaims, StandardCharsets.UTF_8); // ✅ decode here

		String scope = extractQueryParam(url, "scope");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(decodedClaims);

		List<String> mandatory = new ArrayList<>();
		List<String> voluntary = new ArrayList<>();

		extractClaims(root, mandatory, voluntary);

		if (scope != null && scope.contains("profile")) {
			Set<String> standardProfileClaims = Set.of("name", "address", "email", "birthdate", "gender", "picture",
					"phone_number");
			Set<String> allClaims = new HashSet<>();
			allClaims.addAll(mandatory);
			allClaims.addAll(voluntary);

			for (String claim : standardProfileClaims) {
				if (!allClaims.contains(claim)) {
					voluntary.add(claim);
				}
			}
		}

		return Map.of("mandatory", new ArrayList<>(mandatory), "voluntary", new ArrayList<>(voluntary));
	}

	private static void extractClaims(JsonNode node, List<String> mandatory, List<String> voluntary) {
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				JsonNode value = entry.getValue();
				if (value.has("essential")) {
					if (value.get("essential").asBoolean())
						mandatory.add(entry.getKey());
					else
						voluntary.add(entry.getKey());
				}
				extractClaims(value, mandatory, voluntary);
			});
		} else if (node.isArray()) {
			node.forEach(child -> extractClaims(child, mandatory, voluntary));
		}
	}

	private static String extractQueryParam(String url, String param) {
		String[] parts = url.split("[&?]");
		for (String part : parts) {
			if (part.startsWith(param + "=")) {
				return part.substring((param + "=").length());
			}
		}
		return null;
	}

	public static String mapLangToName(String code) {
		return switch (code.toLowerCase()) {
		case "en" -> "English";
		case "hi" -> "Hindi";
		case "ar" -> "Arabic";
		case "kn" -> "Kannada";
		case "ta" -> "Tamil";
		case "km" -> "Khmer";
		default -> code;
		};
	}

	public static String normalizeClaim(String claim) {
		if (claim == null)
			return "";
		String normalized = claim.trim().toLowerCase().replace("_", "").replace(" ", "");
		if (normalized.equals("fullname"))
			return "name";
		if (normalized.equals("emailaddress"))
			return "email";

		return normalized;
	}

	public static List<String> normalizeList(List<String> claims) {
		List<String> normalized = new ArrayList<>();
		for (String claim : claims) {
			normalized.add(normalizeClaim(claim));
		}
		return normalized;
	}

	public static String getDefaultLanguageFromUrl(String url) {
		int start = url.indexOf("ui_locales=") + "ui_locales=".length();
		String lang = url.substring(start);
		if (lang.contains("&")) {
			lang = lang.substring(0, lang.indexOf("&"));
		}
		return URLDecoder.decode(lang, StandardCharsets.UTF_8);
	}

}
