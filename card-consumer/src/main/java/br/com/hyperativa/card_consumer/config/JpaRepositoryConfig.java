package br.com.hyperativa.card_consumer.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.hyperativa.card_common.repository")
@EntityScan(basePackages = "br.com.hyperativa.card_common.entity")
public class JpaRepositoryConfig {
}
