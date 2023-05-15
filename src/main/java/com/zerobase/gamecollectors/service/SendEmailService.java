package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.client.mailgun.MailgunClient;
import com.zerobase.gamecollectors.form.SendEmailForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendEmailService {

    private final MailgunClient mailgunClient;

    public ResponseEntity<String> sendEmail(SendEmailForm form) {
        return mailgunClient.sendEmail(form);
    }
}
