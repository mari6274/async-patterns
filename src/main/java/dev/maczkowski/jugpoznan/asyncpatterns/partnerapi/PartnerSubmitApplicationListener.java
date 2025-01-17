package dev.maczkowski.jugpoznan.asyncpatterns.partnerapi;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!http")
@RabbitListener(queues = "/partnerSubmitApplication/main")
@RequiredArgsConstructor
public class PartnerSubmitApplicationListener {

    private final RabbitTemplate rabbitTemplate;
    private final PartnerApiClient partnerApiClient;

    @RabbitHandler
    public void handleMessage(String application, @Header("backoff-counter") byte backoffCounter) {
        try {
            partnerApiClient.submitApplication(application);
        } catch (Exception e) {
            if (backoffCounter < 2) {
                log.info("Short delaying message: {}", application);
                resendMessage(application, "short", backoffCounter);
            } else if (backoffCounter < 4) {
                log.info("Long delaying message: {}", application);
                resendMessage(application, "long", backoffCounter);
            } else {
                throw new AmqpRejectAndDontRequeueException("Message couldn't be processed", e);
            }
        }
    }

    private void resendMessage(String application, String routingKey, byte backoffCounter) {
        rabbitTemplate.convertAndSend("/partnerSubmitApplication", routingKey, application,
                message -> incrementCounter(message, backoffCounter));
    }

    private static Message incrementCounter(Message message, byte backoffCounter) {
        message.getMessageProperties().setHeader("backoff-counter", backoffCounter + 1);
        return message;
    }
}
