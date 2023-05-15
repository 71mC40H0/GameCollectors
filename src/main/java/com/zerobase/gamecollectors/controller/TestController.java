package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.form.SendEmailForm;
import com.zerobase.gamecollectors.service.SendEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final SendEmailService sendEmailService;

    @PostMapping
    public ResponseEntity<String> sendEmail(@RequestBody SendEmailForm form) {
        return ResponseEntity.ok(sendEmailService.sendEmail(form).getBody());
    }
}
