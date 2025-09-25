package br.com.hyperativa.card_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"br.com.hyperativa.card_common", "br.com.hyperativa.card_consumer"})
public class CardConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardConsumerApplication.class, args);
	}

}
