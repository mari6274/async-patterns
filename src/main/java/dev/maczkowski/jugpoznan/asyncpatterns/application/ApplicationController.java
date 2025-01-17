package dev.maczkowski.jugpoznan.asyncpatterns.application;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final SubmitApplicationProcessor submitApplicationProcessor;
    private final ApplicationRepository applicationRepository;

    @PostMapping("/submit")
    public void submitApplication(@RequestBody Application application) {
        submitApplicationProcessor.process(application);
    }

    @GetMapping("/list")
    public Iterable<ApplicationEntity> listApplications() {
        return applicationRepository.findAll();
    }
}
