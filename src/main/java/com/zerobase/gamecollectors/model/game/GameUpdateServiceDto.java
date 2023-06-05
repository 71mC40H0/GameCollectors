package com.zerobase.gamecollectors.model.game;

import com.zerobase.gamecollectors.common.GameType;
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
public class GameUpdateServiceDto {

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
}
