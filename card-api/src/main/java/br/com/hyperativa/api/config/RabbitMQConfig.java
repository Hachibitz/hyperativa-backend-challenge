package br.com.hyperativa.api.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurações do RabbitMQ específicas para o PRODUTOR (API).
 * Garante que as mensagens sejam enviadas no formato JSON correto.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Define o conversor de mensagens padrão para JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Cria uma instância customizada do RabbitTemplate que utiliza o conversor JSON.
     * Este bean irá sobrescrever o RabbitTemplate padrão do Spring Boot.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}