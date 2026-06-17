package io.mosip.signup.util;
import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.signup.exception.SignUpException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploadFileUtils {
    public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
    private static final String ZIP_SIGNATURE = "504B0304";
    private static final Map<String, String> MAGIC_SIGNATURES = Map.of(
            "89504E47", "image/png",
            "FFD8FF", "image/jpeg",
            "25504446", "application/pdf",
            "D0CF11E0", "application/msword"
    );

    private static final int MAX_ZIP_ENTRIES = 1_024;
    private static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 50L * 1024L * 1024L;
    private static final long MAX_BYTES_PER_ENTRY = 10L * 1024L * 1024L;
    private static final double MAX_COMPRESSION_RATIO = 100d;
    private static final int MAX_ENTRY_NAME_LENGTH = 1_024;

    public static String detectMimeType(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return UNKNOWN_MIME_TYPE;
        }
        // Wrap to support mark/reset for ZIP files
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(Integer.MAX_VALUE);

        byte[] headerBytes = new byte[12];
        int bytesRead = bis.read(headerBytes);

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

        // Check for ZIP-based formats
        if (hexString.startsWith(ZIP_SIGNATURE)) {
            bis.reset();
            return detectOfficeFormat(bis);
        }

        // Check other signatures
        for (Map.Entry<String, String> entry : MAGIC_SIGNATURES.entrySet()) {
            if (hexString.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return UNKNOWN_MIME_TYPE;
    }

    private static String detectOfficeFormat(InputStream inputStream) {
        long totalUncompressed = 0L;
        int entryCount = 0;
        final byte[] drain = new byte[4096];

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                //Zip rejected: entry count exceeded
                if (++entryCount > MAX_ZIP_ENTRIES) {
                    throw new SignUpException(ErrorConstants.INVALID_FILE_TYPE);
                }

                //Zip rejected: suspicious entry name
                String entryName = entry.getName();
                if (entryName == null
                        || entryName.length() > MAX_ENTRY_NAME_LENGTH
                        || entryName.contains("..")
                        || entryName.startsWith("/")
                        || entryName.startsWith("\\")) {
                    throw new SignUpException(ErrorConstants.INVALID_FILE_TYPE);
                }

                // DOCX: contains word/document.xml
                if ("word/document.xml".equalsIgnoreCase(entryName)) {
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
                // PPTX: contains ppt/presentation.xml
                if ("ppt/presentation.xml".equalsIgnoreCase(entryName)) {
                    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                }

                long entryUncompressed = 0L;
                int n;
                while ((n = zis.read(drain)) > 0) {
                    entryUncompressed += n;
                    totalUncompressed += n;

                    //Zip rejected: uncompressed size exceeded
                    if (entryUncompressed > MAX_BYTES_PER_ENTRY
                            || totalUncompressed > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        throw new SignUpException(ErrorConstants.INVALID_FILE_TYPE);
                    }

                    //Zip rejected: compression ratio exceeded
                    long compressed = entry.getCompressedSize();
                    if (compressed > 0
                            && (double) entryUncompressed / (double) compressed > MAX_COMPRESSION_RATIO) {
                        throw new SignUpException(ErrorConstants.INVALID_FILE_TYPE);
                    }
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new SignUpException(ErrorConstants.UPLOAD_FAILED);
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