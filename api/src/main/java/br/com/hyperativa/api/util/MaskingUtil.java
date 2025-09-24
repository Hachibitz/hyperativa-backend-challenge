package br.com.hyperativa.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class MaskingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> SENSITIVE_KEYS = Set.of("password", "email", "cardNumber");

    private MaskingUtil() {
    }

    /**
     * Converte um objeto para uma string JSON, mascarando campos sensíveis.
     * @param object O objeto a ser logado.
     * @return Uma string JSON com dados sensíveis mascarados.
     */
    public static String maskObject(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof MultipartFile file) {
            return String.format("MultipartFile[name=%s, size=%d]", file.getOriginalFilename(), file.getSize());
        }

        try {
            ObjectNode node = objectMapper.valueToTree(object);
            maskNode(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "Error masking object: " + e.getMessage();
        }
    }

    /**
     * Função recursiva para percorrer a árvore JSON e mascarar os campos.
     */
    private static void maskNode(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            if (SENSITIVE_KEYS.contains(fieldName.toLowerCase())) {
                node.put(fieldName, "**********");
            } else if (node.get(fieldName).isObject()) {
                maskNode((ObjectNode) node.get(fieldName));
            }
        });
    }
}
