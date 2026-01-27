package io.mosip.signup.util;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

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
            hexBuilder.append(String.format("%02X", fileBytes[i]));
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

    public static class FileTypeConfig {
        private final Set<String> acceptedFileTypes;
        private final Set<String> fieldNames;

        public FileTypeConfig(Set<String> acceptedFileTypes, Set<String> fieldNames) {
            this.acceptedFileTypes = acceptedFileTypes;
            this.fieldNames = fieldNames;
        }

        public Set<String> getAcceptedFileTypes() {
            return acceptedFileTypes;
        }

        public Set<String> getFieldNames() {
            return fieldNames;
        }
    }
    public static FileTypeConfig extractFileUploadConfig(JsonNode root) {
        Set<String> acceptedFileTypes = new HashSet<>();
        Set<String> fieldNames = new HashSet<>();

        if (root == null) return new FileTypeConfig(acceptedFileTypes, fieldNames);

        JsonNode schemaNode = root.get("schema");
        if (schemaNode == null || !schemaNode.isArray()) return new FileTypeConfig(acceptedFileTypes, fieldNames);

        for (JsonNode field : schemaNode) {
            JsonNode controlTypeNode = field.get("controlType");
            if (controlTypeNode == null || !controlTypeNode.isTextual()) continue;

            String controlType = controlTypeNode.asText().trim();
            if (!controlType.equalsIgnoreCase("photo") && !controlType.equalsIgnoreCase("fileUpload")) {
                continue;
            }

            // Extract field ID
            JsonNode idNode = field.get("id");
            if (idNode != null && idNode.isTextual()) {
                fieldNames.add(idNode.asText().trim());
            }

            // Extract accepted file types
            JsonNode acceptedFileTypesNode = field.get("acceptedFileTypes");
            if (acceptedFileTypesNode == null) continue;

            if (acceptedFileTypesNode.isArray()) {
                for (JsonNode typeNode : acceptedFileTypesNode) {
                    if (typeNode.isTextual()) {
                        String trimmed = typeNode.asText().trim();
                        if (!trimmed.isEmpty()) {
                            acceptedFileTypes.add(trimmed);
                        }
                    }
                }
            } else if (acceptedFileTypesNode.isTextual()) {
                Arrays.stream(acceptedFileTypesNode.asText().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(acceptedFileTypes::add);
            }
        }

        return new FileTypeConfig(acceptedFileTypes, fieldNames);
    }
}