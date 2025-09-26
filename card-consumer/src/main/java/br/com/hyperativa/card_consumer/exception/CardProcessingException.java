package br.com.hyperativa.card_consumer.exception;

public class CardProcessingException extends RuntimeException {
    public CardProcessingException(String message) {
        super(message);
    }
}
