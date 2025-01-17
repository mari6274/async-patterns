package dev.maczkowski.jugpoznan.asyncpatterns;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqQueuesConfiguration {

    @Bean
    public Exchange partnerSubmitApplicationExchange() {
        return ExchangeBuilder.directExchange("/partnerSubmitApplication").build();
    }

    @Bean
    public Queue partnerSubmitApplicationMainQueue() {
        return QueueBuilder.durable("/partnerSubmitApplication/main")
                .deadLetterExchange("/partnerSubmitApplication")
                .deadLetterRoutingKey("dlq")
                .build();
    }

    @Bean
    public Binding partnerSubmitApplicationMainBinding() {
        return BindingBuilder.bind(partnerSubmitApplicationMainQueue())
                .to(partnerSubmitApplicationExchange())
                .with("main")
                .noargs();
    }

    @Bean
    public Queue partnerSubmitApplicationShortQueue() {
        return QueueBuilder.durable("/partnerSubmitApplication/short")
                .deadLetterExchange("/partnerSubmitApplication")
                .deadLetterRoutingKey("main")
                .ttl(5 * 1000)
                .build();
    }

    @Bean
    public Binding partnerSubmitApplicationShortBinding() {
        return BindingBuilder.bind(partnerSubmitApplicationShortQueue())
                .to(partnerSubmitApplicationExchange())
                .with("short")
                .noargs();
    }

    @Bean
    public Queue partnerSubmitApplicationLongQueue() {
        return QueueBuilder.durable("/partnerSubmitApplication/long")
                .deadLetterExchange("/partnerSubmitApplication")
                .deadLetterRoutingKey("main")
                .ttl(10 * 1000)
                .build();
    }

    @Bean
    public Binding partnerSubmitApplicationLongBinding() {
        return BindingBuilder.bind(partnerSubmitApplicationLongQueue())
                .to(partnerSubmitApplicationExchange())
                .with("long")
                .noargs();
    }

    @Bean
    public Queue partnerSubmitApplicationDeadLetterQueue() {
        return QueueBuilder.durable("/partnerSubmitApplication/dlq").build();
    }

    @Bean
    public Binding partnerSubmitApplicationDeadLetterBinding() {
        return BindingBuilder.bind(partnerSubmitApplicationDeadLetterQueue())
                .to(partnerSubmitApplicationExchange())
                .with("dlq")
                .noargs();
    }

}
