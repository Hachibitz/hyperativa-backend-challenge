package br.com.hyperativa.api.exception;

public class EndToEndEncryptionException extends RuntimeException {
    public EndToEndEncryptionException(String message, Throwable e) {
        super(message, e);
    }
}
