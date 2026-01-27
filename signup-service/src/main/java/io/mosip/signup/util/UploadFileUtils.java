package io.mosip.signup.util;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.util.stream.Collectors;

public class UploadFileUtils {

    private static final Map<String, String> MAGIC_SIGNATURES = Map.of(
            "89504E47", "image/png",
            "FFD8FF", "image/jpeg",
            "52494646", "image/webp",
            "25504446", "application/pdf",
            "504B0304", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "D0CF11E0", "application/msword"
    );

    public static String detectMimeType(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 4) {
            return "application/octet-stream";
        }

        StringBuilder hexBuilder = new StringBuilder();
        int bytesToCheck = Math.min(fileBytes.length, 12);

        for (int i = 0; i < bytesToCheck; i++) {
            hexBuilder.append(String.format("%02X", fileBytes[i] & 0xFF));
        }

        String hexString = hexBuilder.toString();

        // Check for WebP specifically (RIFF....WEBP pattern)
        if (hexString.startsWith("52494646") && hexString.length() >= 24
                && hexString.substring(16, 24).equals("57454250")) {
            return "image/webp";
        }

        // Check other signatures
        for (Map.Entry<String, String> entry : MAGIC_SIGNATURES.entrySet()) {
            if (hexString.startsWith(entry.getKey()) && !entry.getKey().equals("52494646")) {
                return entry.getValue();
            }
        }

        return "application/octet-stream";
    }

    public static Set<String> getAcceptedTypesForField(JsonNode root, String targetFieldName) {
        if (root == null || targetFieldName == null || targetFieldName.isBlank()) {
            return Collections.emptySet();
        }

        JsonNode schemaNode = root.get("schema");
        if (schemaNode == null || !schemaNode.isArray()) {
            return Collections.emptySet();
        }

        for (JsonNode field : schemaNode) {
            JsonNode controlTypeNode = field.get("controlType");
            if (controlTypeNode == null || !controlTypeNode.isTextual()) continue;

            String controlType = controlTypeNode.asText().trim();
            if (!controlType.equalsIgnoreCase("photo") && !controlType.equalsIgnoreCase("fileUpload")) {
                continue;
            }

            JsonNode idNode = field.get("id");
            if (idNode == null || !idNode.isTextual()) continue;

            String fieldName = idNode.asText().trim();
            if (!fieldName.equals(targetFieldName)) continue;

            // Found the target field - extract accepted types
            return extractAcceptedTypes(field.get("acceptedFileTypes"));
        }

        return Collections.emptySet();
    }

    private static Set<String> extractAcceptedTypes(JsonNode acceptedFileTypesNode) {
        if (acceptedFileTypesNode == null) return Collections.emptySet();

        Set<String> acceptedTypes = new HashSet<>();

        if (acceptedFileTypesNode.isArray()) {
            for (JsonNode typeNode : acceptedFileTypesNode) {
                if (typeNode.isTextual()) {
                    String trimmed = typeNode.asText().trim();
                    if (!trimmed.isEmpty()) acceptedTypes.add(trimmed);
                }
            }
        } else if (acceptedFileTypesNode.isTextual()) {
            Arrays.stream(acceptedFileTypesNode.asText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(acceptedTypes::add);
        }

        return acceptedTypes.isEmpty() ? Collections.emptySet() : acceptedTypes;
    }
}