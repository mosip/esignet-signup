package io.mosip.signup.util;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public class UiSpecUtils {
    public static Set<String> findAcceptedFileTypes(JsonNode root) {
        Set<String> result = new HashSet<>();
        if (root == null) return result;

        JsonNode schemaNode = root.get("schema");
        if (schemaNode == null || !schemaNode.isArray()) return result;

        for (JsonNode field : schemaNode) {
            JsonNode acceptedFileTypes = field.get("acceptedFileTypes");
            if (acceptedFileTypes == null) continue;

            if (acceptedFileTypes.isArray()) {
                for (JsonNode typeNode : acceptedFileTypes) {
                    if (typeNode.isTextual()) {
                        result.add(typeNode.asText().trim());
                    }
                }
            } else if (acceptedFileTypes.isTextual()) {
                Arrays.stream(acceptedFileTypes.asText().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(result::add);
            }
        }
        return result;
    }
}