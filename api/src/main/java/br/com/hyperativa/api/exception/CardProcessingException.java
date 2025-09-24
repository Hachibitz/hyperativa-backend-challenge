package br.com.hyperativa.api.exception;

public class CardProcessingException extends RuntimeException {
    public CardProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardProcessingException(String message) {
        super(message);
    }
}
