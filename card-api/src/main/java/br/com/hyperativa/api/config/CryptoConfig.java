package br.com.hyperativa.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final static String PRIVATE_KEY_CLASSPATH_LOCATION = "keys/private_key.p8";
    private final static String PUBLIC_KEY_CLASSPATH_LOCATION = "keys/public_key.pub";

    public CryptoConfig() throws Exception {
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        ClassPathResource resource = new ClassPathResource(PRIVATE_KEY_CLASSPATH_LOCATION);
        byte[] keyBytes;
        try (InputStream inputStream = resource.getInputStream()) {
            keyBytes = inputStream.readAllBytes();
        }

        String privateKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\\s", "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        ClassPathResource resource = new ClassPathResource(PUBLIC_KEY_CLASSPATH_LOCATION);
        byte[] keyBytes;
        try (InputStream inputStream = resource.getInputStream()) {
            keyBytes = inputStream.readAllBytes();
        }

        String publicKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\s", "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    @Bean
    public PrivateKey privateKey() {
        return privateKey;
    }

    @Bean
    public PublicKey publicKey() {
        return publicKey;
    }
}
