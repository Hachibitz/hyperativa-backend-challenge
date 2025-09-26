package br.com.hyperativa.api.service;

import br.com.hyperativa.api.model.dto.request.EncryptedCardInsertRequestDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ICardService {
    UploadCardsResponseDTO processCardFile(MultipartFile file);
    void insertSingleCard(EncryptedCardInsertRequestDto encryptedCardInsertRequestDto);
    String checkCardExists(String cardNumber);
}