package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.EndToEndEncryptionException;
import br.com.hyperativa.api.model.dto.request.EncryptCardRequestDto;
import br.com.hyperativa.api.model.dto.response.EncryptCardResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RsaEncryptionServiceTest {

    private RsaEncryptionService rsaEncryptionService;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        this.rsaEncryptionService = new RsaEncryptionService();
    }

    private String decryptData(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Deve criptografar um número de cartão com sucesso usando uma chave pública válida")
    void encryptCard_WithValidPublicKey_ShouldReturnEncryptedData() throws Exception {
        String originalCardNumber = "4242424242424242";
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        EncryptCardRequestDto requestDto = new EncryptCardRequestDto();
        requestDto.setCardNumber(originalCardNumber);
        requestDto.setPublicKey(publicKeyBase64);

        EncryptCardResponseDto responseDto = rsaEncryptionService.encryptCard(requestDto);

        assertNotNull(responseDto);
        assertNotNull(responseDto.getEncryptedCardNumber());
        assertNotEquals(originalCardNumber, responseDto.getEncryptedCardNumber());

        String decryptedCardNumber = decryptData(responseDto.getEncryptedCardNumber());
        assertEquals(originalCardNumber, decryptedCardNumber);
    }

    @Test
    @DisplayName("Deve lançar EndToEndEncryptionException ao usar uma chave pública inválida")
    void encryptCard_WithInvalidPublicKey_ShouldThrowEndToEndEncryptionException() {
        String invalidPublicKey = "chave-publica-invalida";
        EncryptCardRequestDto requestDto = new EncryptCardRequestDto();
        requestDto.setCardNumber("1234567890123456");
        requestDto.setPublicKey(invalidPublicKey);

        EndToEndEncryptionException exception = assertThrows(
                EndToEndEncryptionException.class,
                () -> rsaEncryptionService.encryptCard(requestDto)
        );

        assertEquals("Erro ao criptografar número do cartão", exception.getMessage());
        assertNotNull(exception.getCause());
    }
}