package com.zerobase.gamecollectors.client.mailgun;

import com.zerobase.gamecollectors.form.SendEmailForm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3/")
@Qualifier("mailgun")
public interface MailgunClient {

    @PostMapping("${mailgun.api.domain}" + "/messages")
    ResponseEntity<String> sendEmail(@SpringQueryMap SendEmailForm form);
}
