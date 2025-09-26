package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.EndToEndEncryptionException;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;

@Service
public class RsaDecryptionService {

    private final PrivateKey privateKey;

    public RsaDecryptionService(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EndToEndEncryptionException("Failed to decrypt data", e);
        }
    }
}

