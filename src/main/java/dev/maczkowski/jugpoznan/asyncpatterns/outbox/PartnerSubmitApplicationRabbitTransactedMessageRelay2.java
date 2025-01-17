package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import java.time.Duration;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("rabbit-transacted-one-by-one")
@RequiredArgsConstructor
public class PartnerSubmitApplicationRabbitTransactedMessageRelay2 {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(initialDelay = 5000, fixedDelay = 500)
    public void sendTasksToRabbit() {
        List<OutboxEntity> outboxEntities = outboxRepository.findOutboxEntities(Type.PARTNER_SUBMIT_APPLICATION);
        log.debug("Fetched outbox entities: {}", outboxEntities.size());
        long currentTimeMillis = System.currentTimeMillis();
        outboxEntities.forEach(outboxEntity -> {
                    try {
                        rabbitTemplate.convertAndSend("/partnerSubmitApplication", "main", outboxEntity.getBody(),
                                message -> {
                                    message.getMessageProperties().setHeader("backoff-counter", 0);
                                    return message;
                                });
                        outboxEntity.setStatus(Status.SENT);
                    } catch (RuntimeException e) {
                        log.error(e.getMessage());
                        outboxEntity.setStatus(Status.ERROR);
                    }
                    outboxRepository.save(outboxEntity);
                });

        log.debug("Done in: {}", Duration.ofMillis(System.currentTimeMillis() - currentTimeMillis));
    }
}
