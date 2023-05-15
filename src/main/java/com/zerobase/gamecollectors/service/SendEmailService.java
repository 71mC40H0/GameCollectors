package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.client.mailgun.MailgunClient;
import com.zerobase.gamecollectors.dto.SendEmailServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendEmailService {

    private final MailgunClient mailgunClient;

    public ResponseEntity<String> sendEmail(SendEmailServiceDto serviceDto) {
        return mailgunClient.sendEmail(serviceDto);
    }
}
