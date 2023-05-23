package com.zerobase.gamecollectors.client.mailgun;

import com.zerobase.gamecollectors.model.SendEmailServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3/")
public interface MailgunClient {

    @PostMapping("${mailgun.api.domain}" + "/messages")
    ResponseEntity<String> sendEmail(@SpringQueryMap SendEmailServiceDto serviceDto);
}
