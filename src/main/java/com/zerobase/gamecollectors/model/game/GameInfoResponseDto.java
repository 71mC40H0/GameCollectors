package com.zerobase.gamecollectors.model.game;

import com.zerobase.gamecollectors.domain.entity.Game;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameInfoResponseDto {

    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private int price;
    private int discountRate;
    private int curPrice;
    private LocalDate releaseDate;
    private String platform;
    private String type;
    private String genre;
    private String developer;
    private String publisher;
    private String language;
    private String validCountries;
    private boolean purchasable;

    public static GameInfoResponseDto toResponseDto(Game game) {
        return GameInfoResponseDto.builder()
            .id(game.getId())
            .name(game.getName())
            .imageUrl(game.getImageUrl())
            .description(game.getDescription())
            .price(game.getPrice())
            .discountRate(game.getDiscountRate())
            .curPrice(game.getPrice() * (100 - game.getDiscountRate()) / 100)
            .releaseDate(game.getReleaseDate())
            .platform(game.getPlatform())
            .type(game.getType().toString())
            .genre(game.getGenre())
            .developer(game.getDeveloper())
            .publisher(game.getPublisher())
            .language(game.getLanguage())
            .validCountries(game.getValidCountries())
            .purchasable(game.isPurchasable())
            .build();
    }

}
