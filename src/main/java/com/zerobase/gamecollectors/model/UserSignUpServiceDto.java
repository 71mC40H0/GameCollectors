package com.zerobase.gamecollectors.model;

import com.zerobase.gamecollectors.domain.entity.User;
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
public class UserSignUpServiceDto {

    private String email;
    private String password;
    private String nickname;
    private boolean emailAuth;

    public User toEntity() {
        return User.builder()
            .email(this.getEmail())
            .password(this.getPassword())
            .nickname(this.getNickname())
            .emailAuth(false)
            .build();
    }
}
