package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.gamecollectors.dto.SendEmailServiceDto;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SendEmailServiceTest {

    @Autowired
    private SendEmailService sendEmailService;

    @Value(value = "${mailgun.api.fromEmail}")
    private String fromEmail;

    @Value(value = "${mailgun.api.toEmail}")
    private String toEmail;

    @Value(value = "${mailgun.api.domain}")
    private String mailgunApiDomain;

    @Test
    void sendEmailTest() {
        //given
        SendEmailServiceDto serviceDto = SendEmailServiceDto.builder()
            .from(fromEmail)
            .to(toEmail)
            .subject("Subject of Test Email")
            .text("Text of Test Email")
            .build();

        //when
        String response = sendEmailService.sendEmail(serviceDto).getBody();

        //then
        assertTrue(Objects.requireNonNull(response).contains(mailgunApiDomain));
    }
}