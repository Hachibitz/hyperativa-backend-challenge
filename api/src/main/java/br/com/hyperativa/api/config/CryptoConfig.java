package br.com.hyperativa.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class CryptoConfig {

    private final KeyPair keyPair;

    public CryptoConfig() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.keyPair = generator.generateKeyPair();
    }

    @Bean
    public PrivateKey privateKey() {
        return keyPair.getPrivate();
    }

    @Bean
    public PublicKey publicKey() {
        return keyPair.getPublic();
    }
}
