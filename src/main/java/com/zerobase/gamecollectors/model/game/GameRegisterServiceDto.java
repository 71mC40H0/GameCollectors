package com.zerobase.gamecollectors.model.game;

import com.zerobase.gamecollectors.common.GameType;
import com.zerobase.gamecollectors.domain.entity.Game;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRegisterServiceDto {

    private String name;
    private String imageUrl;
    private String description;
    private int price;
    private int discountRate;
    private LocalDate releaseDate;
    private String platform;
    private GameType type;
    private String genre;
    private String developer;
    private String publisher;
    private String language;
    private String validCountries;
    private boolean purchasable;

    public Game toEntity() {
        return Game.builder()
            .name(this.getName())
            .imageUrl(this.getImageUrl())
            .description(this.getDescription())
            .price(this.getPrice())
            .discountRate(this.getDiscountRate())
            .releaseDate(this.getReleaseDate())
            .platform(this.getPlatform())
            .type(this.getType())
            .genre(this.getGenre())
            .developer(this.getDeveloper())
            .publisher(this.getPublisher())
            .language(this.getLanguage())
            .validCountries(this.getValidCountries())
            .purchasable(this.isPurchasable())
            .build();
    }
}
