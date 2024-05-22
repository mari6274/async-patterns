package dev.maczkowski.jugpoznan.asyncpatterns.partnerapi;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.maczkowski.jugpoznan.asyncpatterns.application.Application;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/partner")
public class PartnerApiMockController {

    @PostMapping("/submitApplication")
    public ResponseEntity<Void> submitApplication(@RequestBody Application application) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(3));
        if (application.incomeAmount() == 100000000) {
            log.error("<PARTNER SYSTEM> error: {}", application);
            return ResponseEntity.internalServerError().build();
        } else {
            log.info("<PARTNER SYSTEM> Application received: {}", application);
            return ResponseEntity.ok().build();
        }
    }
}
