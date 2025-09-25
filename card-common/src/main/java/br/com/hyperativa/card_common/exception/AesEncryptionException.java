package br.com.hyperativa.card_common.exception;

public class AesEncryptionException extends RuntimeException {
    public AesEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
