package br.com.hyperativa.card_consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configurações do RabbitMQ específicas para o SERVIÇO CONSUMER.
 * A topologia principal (fila e exchange) é herdada da lib card-common.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${hyperativa.rabbitmq.queue}")
    private String mainQueueName;

    @Value("${hyperativa.rabbitmq.dlq}")
    private String dlqName;

    @Value("${hyperativa.rabbitmq.exchange-dlx}")
    private String dlxName;

    /**
     * Declara a Dead Letter Exchange (DLX). Isso é específico do consumer.
     */
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxName);
    }

    /**
     * Declara a Dead Letter Queue (DLQ). Específico do consumer.
     */
    @Bean
    Queue deadLetterQueue() {
        return new Queue(dlqName, true);
    }

    /**
     * Cria a ligação entre a DLX e a DLQ. Específico do consumer.
     */
    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(mainQueueName);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory retryContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        RetryTemplate retryTemplate = getRetryTemplate();
        factory.setRetryTemplate(retryTemplate);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }

    private static RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(3.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new org.springframework.retry.policy.SimpleRetryPolicy(3));
        return retryTemplate;
    }
}