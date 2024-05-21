package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnMissingBean(PartnerSubmitApplicationRabbitPublishConfimMessageRelay2.class)
@Profile("rabbit-publish-confirm")
@RequiredArgsConstructor
public class PartnerSubmitApplicationRabbitPublishConfimMessageRelay {

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
                        completableFuture.complete(new CorrelationData.Confirm(false, TEMPLATE_ERROR));
                    }
                    return correlationData;
                })
                .toList();

        Map<Status, List<CorrelationData>> results = correlationDataList.stream()
                .map(cd -> {
                    try {
                        cd.getFuture().get();
                    } catch (Exception e) {
                    }
                    return cd;
                })
                .collect(Collectors.groupingBy(cd -> {
                    try {
                        CorrelationData.Confirm confirm = cd.getFuture().get();
                        if (confirm.isAck()) {
                            return Status.SENT;
                        }
                        if (TEMPLATE_ERROR.equals(confirm.getReason())) {
                            return Status.ERROR;
                        }
                    } catch (Exception e) {
                    }
                    return null;
                }));

        results.entrySet()
                .stream()
                .filter(e -> e.getKey() != null)
                .filter(e -> !e.getValue().isEmpty())
                .forEach(e -> {
                    List<Long> ids = e.getValue().stream()
                            .map(CorrelationData::getId)
                            .map(Long::parseLong)
                            .toList();
                    log.debug("Updating to {}: {}", e.getKey(), ids);
                    outboxRepository.updateSetStatusWhereIdIn(ids, e.getKey());
                });
        log.debug("Done in: {}", Duration.ofMillis(System.currentTimeMillis() - currentTimeMillis));
    }
}
