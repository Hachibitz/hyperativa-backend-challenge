package br.com.hyperativa.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/security")
public class PublicKeyController {

    private final PublicKey publicKey;

    public PublicKeyController(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @GetMapping("/public-key")
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
