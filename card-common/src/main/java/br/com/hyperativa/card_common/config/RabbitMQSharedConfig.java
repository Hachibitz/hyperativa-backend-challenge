package br.com.hyperativa.card_common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ compartilhada entre todos os serviços.
 * Define a topologia principal (exchange, fila principal e binding).
 * As propriedades serão lidas do application.yml de cada serviço que usar esta lib
 * properties para declarar: hyperativa.rabbitmq.queue, hyperativa.rabbitmq.exchange,
 * hyperativa.rabbitmq.routingkey e hyperativa.rabbitmq.exchange-dlx
 */
@Configuration
public class RabbitMQSharedConfig {

    @Value("${hyperativa.rabbitmq.queue}")
    private String queue;

    @Value("${hyperativa.rabbitmq.exchange}")
    private String exchange;

    @Value("${hyperativa.rabbitmq.routingkey}")
    private String routingKey;

    @Value("${hyperativa.rabbitmq.exchange-dlx}")
    private String dlxName;

    /**
     * Declara a exchange principal do tipo Tópico.
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    /**
     * Declara a fila principal JÁ COM OS ARGUMENTOS DE DLQ para que
     * todos os serviços (API e Consumer) tenham a mesma definição.
     */
    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", queue) // Roteia para a DLQ com a mesma chave
                .build();
    }

    /**
     * Cria a ligação (binding) entre a exchange e a fila principal.
     */
    @Bean
    public Binding binding(@Qualifier("mainQueue") Queue mainQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(mainQueue)
                .to(exchange)
                .with(routingKey);
    }
}