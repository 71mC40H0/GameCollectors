package com.zerobase.gamecollectors.domain.entity;

import com.zerobase.gamecollectors.common.GameType;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditOverride;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Game extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    private String imageUrl;
    private String description;

    @Column(columnDefinition = "int default 0")
    @Min(0)
    private int price;

    @Column(columnDefinition = "int default 0")
    @Min(0)
    @Max(100)
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
