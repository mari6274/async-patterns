package dev.maczkowski.jugpoznan.asyncpatterns;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@EnableRabbit
@Profile("rabbit-publish-confirm")
@Configuration
public class RabbitMqTemplatePublishConfirmConfiguration {

}
