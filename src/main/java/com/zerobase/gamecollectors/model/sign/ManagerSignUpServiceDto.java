package com.zerobase.gamecollectors.model.sign;

import com.zerobase.gamecollectors.domain.entity.Manager;
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
public class ManagerSignUpServiceDto {

    private String email;
    private String password;
    private boolean emailAuth;


    public Manager toEntity() {
        return Manager.builder()
            .email(this.getEmail())
            .password(this.getPassword())
            .emailAuth(false)
            .build();
    }
}
