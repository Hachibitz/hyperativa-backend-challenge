package br.com.hyperativa.card_consumer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${hyperativa.rabbitmq.queue}")
    private String queue;

    /**
     * Declara a fila que o consumer irá escutar.
     * Se a fila já existir no broker do RabbitMQ, esta declaração é ignorada (idempotente).
     * Se não existir, ela é criada.
     * @return A instância da fila.
     */
    @Bean
    public Queue queue() {
        return new Queue(queue, true);
    }
}