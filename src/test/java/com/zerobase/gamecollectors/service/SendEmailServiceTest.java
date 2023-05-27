package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.gamecollectors.model.SendEmailServiceDto;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SendEmailServiceTest {

    @Autowired
    private SendEmailService sendEmailService;

    @Value(value = "${mailgun.api.emailSender}")
    private String emailSender;

    @Value(value = "${mailgun.api.emailReceiver}")
    private String emailReceiver;

    @Value(value = "${mailgun.api.domain}")
    private String mailgunApiDomain;

    @Test
    @DisplayName("Mailgun 이메일 전송")
    void sendEmailTest() {
        //given
        SendEmailServiceDto serviceDto = SendEmailServiceDto.builder()
            .from(emailSender)
            .to(emailReceiver)
            .subject("Subject of Test Email")
            .text("Text of Test Email")
            .build();

        //when
        String response = sendEmailService.sendEmail(serviceDto).getBody();

        //then
        assertTrue(Objects.requireNonNull(response).contains(mailgunApiDomain));
    }
}