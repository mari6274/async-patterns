package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("rabbit-publish-confirm-one-by-one")
@RequiredArgsConstructor
public class PartnerSubmitApplicationRabbitPublishConfimMessageRelay2 {

    public static final String TEMPLATE_ERROR = "TemplateError";
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(initialDelay = 5000, fixedDelay = 500)
    public void sendTasksToRabbit() {
        List<OutboxEntity> outboxEntities = outboxRepository.findOutboxEntities(Type.PARTNER_SUBMIT_APPLICATION);
        log.debug("Fetched outbox entities: {}", outboxEntities.size());
        long currentTimeMillis = System.currentTimeMillis();

        List<CorrelationData> correlationDataList = outboxEntities
                .stream()
                .map(outboxEntity -> {
                    CorrelationData correlationData = new CorrelationData(outboxEntity.getId().toString());
                    CompletableFuture<CorrelationData.Confirm> completableFuture = correlationData.getFuture();
                    completableFuture
                            .whenComplete((confirm, throwable) -> {
                                if (confirm.isAck()) {
                                    log.debug("ACK for: {}", outboxEntity.getId());
                                    outboxEntity.setStatus(Status.SENT);
                                    outboxRepository.save(outboxEntity);
                                }
                            })
                            .orTimeout(10, TimeUnit.SECONDS);
                    try {
                        rabbitTemplate.convertAndSend("/partnerSubmitApplication", "main", outboxEntity.getBody(),
                                message -> {
                                    message.getMessageProperties().setHeader("backoff-counter", 0);
                                    return message;
                                }, correlationData);
                    } catch (RuntimeException e) {
                        log.error(e.getMessage());
                        outboxEntity.setStatus(Status.ERROR);
                        outboxRepository.save(outboxEntity);
                        completableFuture.complete(new CorrelationData.Confirm(false, TEMPLATE_ERROR));
                    }
                    return correlationData;
                })
                .toList();

        correlationDataList.stream()
                .forEach(cd -> {
                    try {
                        cd.getFuture().get();
                    } catch (Exception e) {
                    }
                });
        log.debug("Done in: {}", Duration.ofMillis(System.currentTimeMillis() - currentTimeMillis));
    }
}
