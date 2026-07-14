package io.mosip.signup.util;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.StreamSupport;

public class UploadFileUtils {
    public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
    private static final Map<String, String> MAGIC_SIGNATURES = Map.of(
            "89504E47", "image/png",
            "FFD8FF", "image/jpeg",
            "25504446", "application/pdf",
            "D0CF11E0", "application/msword"
    );

    public static String detectMimeType(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return UNKNOWN_MIME_TYPE;
        }

        byte[] headerBytes = new byte[12];
        int bytesRead = inputStream.read(headerBytes);

        if (bytesRead < 4) {
            return UNKNOWN_MIME_TYPE;
        }

        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < bytesRead; i++) {
            hexBuilder.append(String.format("%02X", headerBytes[i] & 0xFF));
        }

        String hexString = hexBuilder.toString();

        // Check for WebP specifically (RIFF....WEBP pattern)
        if (hexString.startsWith("52494646") && hexString.length() >= 24
                && hexString.substring(16, 24).equals("57454250")) {
            return "image/webp";
        }

        // Check other signatures
        for (Map.Entry<String, String> entry : MAGIC_SIGNATURES.entrySet()) {
            if (hexString.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return UNKNOWN_MIME_TYPE;
    }


    public static Set<String> getAcceptedTypesForField(JsonNode root, String targetFieldName) {
        if (root == null || targetFieldName == null || targetFieldName.isBlank()) {
            return Collections.emptySet();
        }

        JsonNode schemaNode = root.get("schema");
        if (schemaNode == null || !schemaNode.isArray()) {
            return Collections.emptySet();
        }

        return StreamSupport.stream(schemaNode.spliterator(), false)
                .filter(UploadFileUtils::isFileUploadField)
                .filter(field -> targetFieldName.equals(getFieldId(field)))
                .findFirst()
                .map(field -> extractAcceptedTypes(field.get("acceptedFileTypes")))
                .orElse(Collections.emptySet());
    }

    private static boolean isFileUploadField(JsonNode field) {
        JsonNode controlTypeNode = field.get("controlType");
        if (controlTypeNode == null || !controlTypeNode.isTextual()) {
            return false;
        }
        String controlType = controlTypeNode.asText().trim();
        return controlType.equalsIgnoreCase("photo") || controlType.equalsIgnoreCase("fileUpload");
    }

    private static String getFieldId(JsonNode field) {
        JsonNode idNode = field.get("id");
        if (idNode == null || !idNode.isTextual()) {
            return null;
        }
        return idNode.asText().trim();
    }

    private static Set<String> extractAcceptedTypes(JsonNode acceptedFileTypesNode) {
        if (acceptedFileTypesNode == null) return Collections.emptySet();

        Set<String> acceptedTypes = new HashSet<>();

        if (acceptedFileTypesNode.isArray()) {
            for (JsonNode typeNode : acceptedFileTypesNode) {
                if (typeNode.isTextual()) {
                    String trimmed = typeNode.asText().trim().toLowerCase(Locale.ROOT);
                    if (!trimmed.isEmpty()) acceptedTypes.add(trimmed);
                }
            }
        } else if (acceptedFileTypesNode.isTextual()) {
            Arrays.stream(acceptedFileTypesNode.asText().split(","))
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .filter(s -> !s.isEmpty())
                    .forEach(acceptedTypes::add);
        }

        return acceptedTypes.isEmpty() ? Collections.emptySet() : acceptedTypes;
    }
}