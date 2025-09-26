package br.com.hyperativa.card_common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;

public final class MaskingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> SENSITIVE_KEYS = Set.of("password", "email", "cardNumber");

    private MaskingUtil() {
    }

    /**
     * NOVO MÉTODO: Otimizado para mascarar uma string JSON diretamente.
     * Perfeito para o consumer, pois evita a desserialização/resserialização desnecessária
     * apenas para o log.
     *
     * @param jsonString A string JSON de entrada.
     * @return Uma nova string JSON com os campos sensíveis mascarados.
     */
    public static String maskJsonString(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return jsonString;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            if (rootNode.isObject()) {
                maskNode((ObjectNode) rootNode);
            }
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return "{\"error\":\"Could not mask JSON message\"}";
        }
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
