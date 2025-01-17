package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import dev.maczkowski.jugpoznan.asyncpatterns.partnerapi.PartnerApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("http")
@RequiredArgsConstructor
public class PartnerSubmitApplicationHttpMessageRelay {

    private final OutboxRepository outboxRepository;
    private final PartnerApiClient partnerApiClient;

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void sendTasks() {
        outboxRepository.findOutboxEntities(Type.PARTNER_SUBMIT_APPLICATION)
                .forEach(outboxEntity -> {
                    try {
                        partnerApiClient.submitApplication(outboxEntity.getBody());
                        outboxEntity.setStatus(Status.SENT);
                    } catch (HttpServerErrorException e) {
                        outboxEntity.incrementAttempts();
                        if (outboxEntity.getAttempts() == 5) {
                            outboxEntity.setStatus(Status.ERROR);
                        }
                    } catch (RuntimeException e) {
                        log.error(e.getMessage());
                        outboxEntity.setStatus(Status.ERROR);
                    } finally {
                        outboxRepository.save(outboxEntity);
                    }
                });
    }

}
