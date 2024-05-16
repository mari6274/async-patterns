package dev.maczkowski.jugpoznan.asyncpatterns.application;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.maczkowski.jugpoznan.asyncpatterns.outbox.OutboxEntity;
import dev.maczkowski.jugpoznan.asyncpatterns.outbox.OutboxRepository;
import dev.maczkowski.jugpoznan.asyncpatterns.outbox.Type;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitApplicationProcessor {

    private final ApplicationRepository applicationRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional(rollbackOn = Exception.class)
    void process(Application application) {
        ApplicationEntity applicationEntity = new ApplicationEntity(null, null,
                application.firstName(),
                application.lastName(),
                application.profession(),
                application.incomeAmount());
        applicationRepository.save(applicationEntity);

        OutboxEntity outboxEntity = new OutboxEntity(Type.PARTNER_SUBMIT_APPLICATION,
                objectMapper.writeValueAsString(application));
        outboxRepository.save(outboxEntity);
    }
}
