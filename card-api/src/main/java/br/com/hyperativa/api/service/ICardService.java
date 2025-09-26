package br.com.hyperativa.api.service;

import br.com.hyperativa.api.model.dto.request.EncryptedCardInsertRequestDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ICardService {
    UploadCardsResponseDTO processCardFile(MultipartFile file, Boolean isToUseLuhnAlg);
    void insertSingleCard(EncryptedCardInsertRequestDto encryptedCardInsertRequestDto, Boolean isToUseLuhnAlg);
    String checkCardExists(String cardNumber);
}