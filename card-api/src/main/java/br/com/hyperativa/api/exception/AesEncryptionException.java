package br.com.hyperativa.api.exception;

public class AesEncryptionException extends RuntimeException {
    public AesEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
