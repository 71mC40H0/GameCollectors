package com.zerobase.gamecollectors.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SendEmailForm {

    private String from;
    private String to;
    private String subject;
    private String text;

}
