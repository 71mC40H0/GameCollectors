package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zerobase.gamecollectors.common.GameType;
import com.zerobase.gamecollectors.domain.entity.Game;
import com.zerobase.gamecollectors.domain.repository.GameRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.game.GameInfoResponseDto;
import com.zerobase.gamecollectors.model.game.GameRegisterServiceDto;
import com.zerobase.gamecollectors.model.game.GameUpdateServiceDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagerGameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private ManagerGameService managerGameService;

    @Test
    @DisplayName("게임 등록 성공")
    void testRegisterGameSuccess() {
        //given
        GameRegisterServiceDto dto = GameRegisterServiceDto.builder()
            .name("게임명")
            .imageUrl("게임 이미지 URL")
            .description("게임 설명")
            .price(1000000)
            .discountRate(0)
            .releaseDate(LocalDate.of(2023, 2, 27))
            .platform("플랫폼")
            .type(GameType.GAME)
            .genre("장르")
            .developer("개발사")
            .publisher("퍼블리셔")
            .language("지원 언어")
            .validCountries("지원 국가")
            .purchasable(true)
            .build();

        ArgumentCaptor<Game> argumentCaptor = ArgumentCaptor.forClass(Game.class);

        //when
        managerGameService.registerGame(dto);

        //then
        verify(gameRepository).save(argumentCaptor.capture());
    }

    @Test
    @DisplayName("게임 수정 성공")
    void testUpdateGameSuccess() {
        //given
        GameUpdateServiceDto dto = GameUpdateServiceDto.builder()
            .name("게임명2")
            .imageUrl("게임 이미지 URL2")
            .description("게임 설명2")
            .price(2000000)
            .discountRate(50)
            .releaseDate(LocalDate.of(2023, 7, 7))
            .platform("플랫폼2")
            .type(GameType.BUNDLE)
            .genre("장르2")
            .developer("개발사2")
            .publisher("퍼블리셔2")
            .language("지원 언어2")
            .validCountries("지원 국가2")
            .purchasable(false)
            .build();

        Game game = Game.builder()
            .id(1L)
            .name("게임명")
            .imageUrl("게임 이미지 URL")
            .description("게임 설명")
            .price(1000000)
            .discountRate(0)
            .releaseDate(LocalDate.of(2023, 2, 27))
            .platform("플랫폼")
            .type(GameType.GAME)
            .genre("장르")
            .developer("개발사")
            .publisher("퍼블리셔")
            .language("지원 언어")
            .validCountries("지원 국가")
            .purchasable(true)
            .build();

        given(gameRepository.findById(anyLong())).willReturn(Optional.of(game));
        //when
        managerGameService.updateGame(1L, dto);

        //then
        assertEquals(1L, game.getId());
        assertEquals("게임명2", game.getName());
        assertEquals("게임 이미지 URL2", game.getImageUrl());
        assertEquals("게임 설명2", game.getDescription());
        assertEquals(2000000, game.getPrice());
        assertEquals(50, game.getDiscountRate());
        assertEquals(LocalDate.of(2023, 7, 7), game.getReleaseDate());
        assertEquals("플랫폼2", game.getPlatform());
        assertEquals(GameType.BUNDLE, game.getType());
        assertEquals("장르2", game.getGenre());
        assertEquals("개발사2", game.getDeveloper());
        assertEquals("퍼블리셔2", game.getPublisher());
        assertEquals("지원 언어2", game.getLanguage());
        assertEquals("지원 국가2", game.getValidCountries());
        assertFalse(game.isPurchasable());
    }

    @Test
    @DisplayName("게임 수정 실패 - 존재하지 않는 게임")
    void testUpdateGameSuccess_NotFoundGame() {
        //given
        GameUpdateServiceDto dto = GameUpdateServiceDto.builder()
            .name("게임명2")
            .imageUrl("게임 이미지 URL2")
            .description("게임 설명2")
            .price(2000000)
            .discountRate(50)
            .releaseDate(LocalDate.of(2023, 7, 7))
            .platform("플랫폼2")
            .type(GameType.BUNDLE)
            .genre("장르2")
            .developer("개발사2")
            .publisher("퍼블리셔2")
            .language("지원 언어2")
            .validCountries("지원 국가2")
            .purchasable(false)
            .build();

        given(gameRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class, () -> managerGameService.updateGame(1L, dto));

        //then
        assertEquals(ErrorCode.NOT_FOUND_GAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("게임 전체 조회 성공")
    void testGetInfosSuccess() {
        //given
        List<Game> games = new ArrayList<>();

        Game game1 = Game.builder()
            .id(1L)
            .name("게임명")
            .imageUrl("게임 이미지 URL")
            .description("게임 설명")
            .price(1000000)
            .discountRate(0)
            .releaseDate(LocalDate.of(2023, 2, 27))
            .platform("플랫폼")
            .type(GameType.GAME)
            .genre("장르")
            .developer("개발사")
            .publisher("퍼블리셔")
            .language("지원 언어")
            .validCountries("지원 국가")
            .purchasable(true)
            .build();

        Game game2 = Game.builder()
            .id(2L)
            .name("게임명2")
            .imageUrl("게임 이미지 URL2")
            .description("게임 설명2")
            .price(2000000)
            .discountRate(20)
            .releaseDate(LocalDate.of(2023, 2, 2))
            .platform("플랫폼2")
            .type(GameType.DLC)
            .genre("장르2")
            .developer("개발사2")
            .publisher("퍼블리셔2")
            .language("지원 언어2")
            .validCountries("지원 국가2")
            .purchasable(false)
            .build();

        games.add(game1);
        games.add(game2);

        given(gameRepository.findByDeletedAtIsNull()).willReturn(games);

        //when
        List<GameInfoResponseDto> result = managerGameService.getInfos();

        //then
        for (int i = 0; i < games.size(); i++) {
            GameInfoResponseDto expectedDto = GameInfoResponseDto.toResponseDto(games.get(i));
            GameInfoResponseDto actualDto = result.get(i);
            assertEquals(actualDto.getId(), expectedDto.getId());
            assertEquals(actualDto.getName(), expectedDto.getName());
            assertEquals(actualDto.getImageUrl(), expectedDto.getImageUrl());
            assertEquals(actualDto.getDescription(), expectedDto.getDescription());
            assertEquals(actualDto.getPrice(), expectedDto.getPrice());
            assertEquals(actualDto.getDiscountRate(), expectedDto.getDiscountRate());
            assertEquals(actualDto.getCurPrice(), expectedDto.getCurPrice());
            assertEquals(actualDto.getReleaseDate(), expectedDto.getReleaseDate());
            assertEquals(actualDto.getPlatform(), expectedDto.getPlatform());
            assertEquals(actualDto.getType(), expectedDto.getType());
            assertEquals(actualDto.getGenre(), expectedDto.getGenre());
            assertEquals(actualDto.getDeveloper(), expectedDto.getDeveloper());
            assertEquals(actualDto.getPublisher(), expectedDto.getPublisher());
            assertEquals(actualDto.getLanguage(), expectedDto.getLanguage());
            assertEquals(actualDto.getValidCountries(), expectedDto.getValidCountries());
            assertEquals(actualDto.isPurchasable(), expectedDto.isPurchasable());
        }
    }

    @Test
    @DisplayName("게임 조회 성공")
    void testGetInfoSuccess() {
        //given
        Game game = Game.builder()
            .id(1L)
            .name("게임명")
            .imageUrl("게임 이미지 URL")
            .description("게임 설명")
            .price(1000000)
            .discountRate(0)
            .releaseDate(LocalDate.of(2023, 2, 27))
            .platform("플랫폼")
            .type(GameType.GAME)
            .genre("장르")
            .developer("개발사")
            .publisher("퍼블리셔")
            .language("지원 언어")
            .validCountries("지원 국가")
            .purchasable(true)
            .build();

        given(gameRepository.findByIdAndDeletedAtIsNotNull(anyLong())).willReturn(Optional.ofNullable(game));

        //when
        GameInfoResponseDto result = managerGameService.getInfo(1L);

        //then
        assertEquals(Objects.requireNonNull(game).getId(), result.getId());
        assertEquals(game.getName(), result.getName());
        assertEquals(game.getImageUrl(), result.getImageUrl());
        assertEquals(game.getDescription(), result.getDescription());
        assertEquals(game.getPrice(), result.getPrice());
        assertEquals(game.getDiscountRate(), result.getDiscountRate());
        assertEquals(game.getPrice() * (100 - game.getDiscountRate()) / 100, result.getCurPrice());
        assertEquals(game.getReleaseDate(), result.getReleaseDate());
        assertEquals(game.getPlatform(), result.getPlatform());
        assertEquals(game.getType().toString(), result.getType());
        assertEquals(game.getGenre(), result.getGenre());
        assertEquals(game.getDeveloper(), result.getDeveloper());
        assertEquals(game.getPublisher(), result.getPublisher());
        assertEquals(game.getLanguage(), result.getLanguage());
        assertEquals(game.getValidCountries(), result.getValidCountries());
        assertEquals(game.isPurchasable(), result.isPurchasable());
    }

    @Test
    @DisplayName("게임 조회 실패 - 존재하지 않는 게임")
    void testGetInfoFail_NotFoundGame() {
        //given
        given(gameRepository.findByIdAndDeletedAtIsNotNull(anyLong())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class, () -> managerGameService.getInfo(1L));

        //then
        assertEquals(ErrorCode.NOT_FOUND_GAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("게임 삭제 성공")
    void testDeleteGameSuccess() {
        //given
        Game game = Game.builder()
            .id(1L)
            .name("게임명")
            .imageUrl("게임 이미지 URL")
            .description("게임 설명")
            .price(1000000)
            .discountRate(0)
            .releaseDate(LocalDate.of(2023, 2, 27))
            .platform("플랫폼")
            .type(GameType.GAME)
            .genre("장르")
            .developer("개발사")
            .publisher("퍼블리셔")
            .language("지원 언어")
            .validCountries("지원 국가")
            .purchasable(true)
            .build();

        given(gameRepository.findByIdAndDeletedAtIsNotNull(anyLong())).willReturn(Optional.ofNullable(game));

        //when
        managerGameService.deleteGame(1L);

        //then
        assertNotNull(Objects.requireNonNull(game).getDeletedAt());
    }

    @Test
    @DisplayName("게임 삭제 실패 - 존재하지 않는 게임")
    void testDeleteGameFail_NotFoundGame() {
        //given
        given(gameRepository.findByIdAndDeletedAtIsNotNull(anyLong())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class, () -> managerGameService.getInfo(1L));

        //then
        assertEquals(ErrorCode.NOT_FOUND_GAME, exception.getErrorCode());
    }
}