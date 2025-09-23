package br.com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AesEncryptionException extends RuntimeException {
    public AesEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
