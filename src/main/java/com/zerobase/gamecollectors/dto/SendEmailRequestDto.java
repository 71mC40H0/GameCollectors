package com.zerobase.gamecollectors.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SendEmailRequestDto {

    private String from;
    private String to;
    private String subject;
    private String text;

    public SendEmailServiceDto toServiceDto() {
        return SendEmailServiceDto.builder()
            .from(this.from)
            .to(this.getTo())
            .subject(this.getSubject())
            .text(this.getText())
            .build();
    }

}
