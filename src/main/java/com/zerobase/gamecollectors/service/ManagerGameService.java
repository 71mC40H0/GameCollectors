package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.domain.entity.Game;
import com.zerobase.gamecollectors.domain.repository.GameRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.game.GameInfoResponseDto;
import com.zerobase.gamecollectors.model.game.GameRegisterServiceDto;
import com.zerobase.gamecollectors.model.game.GameUpdateServiceDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerGameService {

    private final GameRepository gameRepository;

    @Transactional
    public void registerGame(GameRegisterServiceDto dto) {
        gameRepository.save(dto.toEntity());
    }

    @Transactional
    public void updateGame(Long id, GameUpdateServiceDto dto) {
        Game game = gameRepository.findByIdAndDeletedAtIsNotNull(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_GAME));

        game.setName(dto.getName());
        game.setImageUrl(dto.getImageUrl());
        game.setDescription(dto.getDescription());
        game.setPrice(dto.getPrice());
        game.setDiscountRate(dto.getDiscountRate());
        game.setReleaseDate(dto.getReleaseDate());
        game.setPlatform(dto.getPlatform());
        game.setType(dto.getType());
        game.setGenre(dto.getGenre());
        game.setDeveloper(dto.getDeveloper());
        game.setPublisher(dto.getPublisher());
        game.setLanguage(dto.getLanguage());
        game.setValidCountries(dto.getValidCountries());
        game.setPurchasable(dto.isPurchasable());
    }

    public List<GameInfoResponseDto> getInfos() {

        List<Game> games = gameRepository.findByDeletedAtIsNull();

        return games.stream().map(GameInfoResponseDto::toResponseDto).collect(Collectors.toList());
    }

    public GameInfoResponseDto getInfo(Long id) {
        Game game = gameRepository.findByIdAndDeletedAtIsNotNull(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_GAME));

        return GameInfoResponseDto.toResponseDto(game);
    }

    @Transactional
    public void deleteGame(Long id) {
        Game game = gameRepository.findByIdAndDeletedAtIsNotNull(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_GAME));

        game.setDeletedAt(LocalDateTime.now());
    }
}
