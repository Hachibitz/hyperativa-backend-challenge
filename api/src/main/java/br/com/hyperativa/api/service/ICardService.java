package br.com.hyperativa.api.service;

import br.com.hyperativa.api.model.dto.CardDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ICardService {
    UploadCardsResponseDTO processCardFile(MultipartFile file);
    void insertSingleCard(CardDto cardDto);
    String checkCardExists(String cardNumber);
}