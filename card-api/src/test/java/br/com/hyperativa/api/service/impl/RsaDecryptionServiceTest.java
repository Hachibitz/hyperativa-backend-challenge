package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.EndToEndEncryptionException;
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

class RsaDecryptionServiceTest {

    private RsaDecryptionService rsaDecryptionService;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        this.rsaDecryptionService = new RsaDecryptionService(privateKey);
    }

    private String encryptData(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Test
    @DisplayName("Deve descriptografar dados válidos com sucesso")
    void decrypt_WithValidEncryptedData_ShouldReturnDecryptedString() throws Exception {
        String originalData = "4242424242424242";
        String encryptedData = encryptData(originalData);

        String decryptedData = rsaDecryptionService.decrypt(encryptedData);

        assertNotNull(decryptedData);
        assertEquals(originalData, decryptedData);
    }

    @Test
    @DisplayName("Deve lançar EndToEndEncryptionException para dados inválidos")
    void decrypt_WithInvalidData_ShouldThrowEndToEndEncryptionException() {
        String invalidEncryptedData = "dados-invalidos-que-nao-sao-base64-ou-criptografados";

        EndToEndEncryptionException exception = assertThrows(
                EndToEndEncryptionException.class,
                () -> rsaDecryptionService.decrypt(invalidEncryptedData)
        );

        assertEquals("Failed to decrypt data", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar EndToEndEncryptionException para dados corrompidos")
    void decrypt_WithCorruptedData_ShouldThrowEndToEndEncryptionException() {
        String corruptedBase64Data = Base64.getEncoder().encodeToString("corrompido".getBytes());

        assertThrows(
                EndToEndEncryptionException.class,
                () -> rsaDecryptionService.decrypt(corruptedBase64Data)
        );
    }
}