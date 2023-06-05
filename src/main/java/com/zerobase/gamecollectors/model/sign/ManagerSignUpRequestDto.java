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
@ApiModel(description = "관리자 회원가입 양식")
public class ManagerSignUpRequestDto {

    @Email
    @ApiModelProperty(value = "관리자 이메일", required = true)
    private String email;
    @ApiModelProperty(value = "관리자 비밀번호", required = true)
    private String password;

    public ManagerSignUpServiceDto toServiceDto() {
        return ManagerSignUpServiceDto.builder()
            .email(this.getEmail())
            .password(this.getPassword())
            .emailAuth(false)
            .build();
    }
}
