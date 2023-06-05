package com.zerobase.gamecollectors.model.game;

import com.zerobase.gamecollectors.common.GameType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "게임 등록 양식")
public class GameRegisterRequestDto {

    @ApiModelProperty(value = "이름", required = true)
    private String name;
    @ApiModelProperty(value = "이미지 경로")
    private String imageUrl;
    @ApiModelProperty(value = "상세정보")
    private String description;
    @ApiModelProperty(value = "정가")
    private int price;
    @ApiModelProperty(value = "할인율")
    private int discountRate;
    @ApiModelProperty(value = "발매일")
    private LocalDate releaseDate;
    @ApiModelProperty(value = "플랫폼")
    private String platform;
    @ApiModelProperty(value = "게임종류")
    private GameType type;
    @ApiModelProperty(value = "장르")
    private String genre;
    @ApiModelProperty(value = "개발사")
    private String developer;
    @ApiModelProperty(value = "퍼블리셔")
    private String publisher;
    @ApiModelProperty(value = "지원 언어")
    private String language;
    @ApiModelProperty(value = "구매 가능 국가")
    private String validCountries;
    @ApiModelProperty(value = "구매 가능 여부")
    private boolean purchasable;

    public GameRegisterServiceDto toServiceDto() {
        return GameRegisterServiceDto.builder()
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
