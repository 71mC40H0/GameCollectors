package com.zerobase.gamecollectors.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "로그인 양식")
public class SignInRequestDto {

    @Email
    @ApiModelProperty(value = "이메일", required = true)
    private String email;
    @ApiModelProperty(value = "비밀번호", required = true)
    private String password;

    public SignInServiceDto toServiceDto() {
        return SignInServiceDto.builder()
            .email(this.getEmail())
            .password(this.getPassword())
            .build();
    }
}
