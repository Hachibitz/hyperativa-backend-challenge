package br.com.hyperativa.card_common.service.impl;

import br.com.hyperativa.card_common.exception.AesEncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String PLAIN_TEXT_CARD_NUMBER = "4242424242424242";

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        String secretKey = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
        encryptionService = new EncryptionService(secretKey);
    }

    @Test
    @DisplayName("Deve criptografar e descriptografar os dados, retornando o texto original")
    void encryptAndDecrypt_WhenCalledSequentially_ShouldReturnOriginalText() {
        String encryptedText = encryptionService.encrypt(PLAIN_TEXT_CARD_NUMBER);
        String decryptedText = encryptionService.decrypt(encryptedText);

        assertNotNull(encryptedText);
        assertNotEquals(PLAIN_TEXT_CARD_NUMBER, encryptedText);
        assertEquals(PLAIN_TEXT_CARD_NUMBER, decryptedText);
    }

    @Test
    @DisplayName("Deve retornar nulo ao tentar criptografar uma string nula")
    void encrypt_WithNullInput_ShouldThrowException() {
        assertThrows(AesEncryptionException.class, () -> {
            encryptionService.encrypt(null);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar descriptografar dados corrompidos")
    void decrypt_WithCorruptedCipherText_ShouldThrowAesEncryptionException() {
        String encryptedText = encryptionService.encrypt(PLAIN_TEXT_CARD_NUMBER);
        String corruptedText = encryptedText.substring(0, encryptedText.length() - 4) + "XXXX";

        assertThrows(AesEncryptionException.class, () -> {
            encryptionService.decrypt(corruptedText);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar descriptografar uma string que não é Base64")
    void decrypt_WithInvalidBase64_ShouldThrowAesEncryptionException() {
        String invalidBase64 = "isso-nao-e-base64";

        assertThrows(AesEncryptionException.class, () -> {
            encryptionService.decrypt(invalidBase64);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção no construtor se a chave secreta for inválida")
    void constructor_WithInvalidBase64Key_ShouldThrowException() {
        String invalidSecret = "chave-invalida";

        assertThrows(IllegalArgumentException.class, () -> {
            new EncryptionService(invalidSecret);
        });
    }
}