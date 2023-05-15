package com.zerobase.gamecollectors.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SendEmailServiceDto {

    private String from;
    private String to;
    private String subject;
    private String text;

}
