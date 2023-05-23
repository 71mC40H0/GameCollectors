package com.zerobase.gamecollectors.model;

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
            .from(this.getFrom())
            .to(this.getTo())
            .subject(this.getSubject())
            .text(this.getText())
            .build();
    }

}
