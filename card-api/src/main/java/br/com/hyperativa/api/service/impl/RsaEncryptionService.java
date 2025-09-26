package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.EndToEndEncryptionException;
import br.com.hyperativa.api.model.dto.request.EncryptCardRequestDto;
import br.com.hyperativa.api.model.dto.response.EncryptCardResponseDto;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class RsaEncryptionService {

    public EncryptCardResponseDto encryptCard(EncryptCardRequestDto encryptCardRequestDto) {
        try {
            String publicKeyBase64 = encryptCardRequestDto.getPublicKey();
            String cardNumber = encryptCardRequestDto.getCardNumber();

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            return new EncryptCardResponseDto(encryptedBase64);

        } catch (Exception e) {
            throw new EndToEndEncryptionException("Erro ao criptografar número do cartão", e);
        }
    }
}
