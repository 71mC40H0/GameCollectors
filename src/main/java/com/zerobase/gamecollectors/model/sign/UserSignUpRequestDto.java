package com.zerobase.gamecollectors.model.sign;

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
@ApiModel(description = "사용자 회원가입 양식")
public class UserSignUpRequestDto {

    @Email
    @ApiModelProperty(value = "사용자 이메일", required = true)
    private String email;

    @ApiModelProperty(value = "사용자 비밀번호", required = true)
    private String password;

    @ApiModelProperty(value = "사용자 닉네임", required = true)
    private String nickname;

    public UserSignUpServiceDto toServiceDto() {
        return UserSignUpServiceDto.builder()
            .email(this.getEmail())
            .password(this.getPassword())
            .nickname(this.getNickname())
            .emailAuth(false)
            .build();
    }
}