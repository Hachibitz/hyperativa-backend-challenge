package br.com.hyperativa.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"br.com.hyperativa.card_common.repository", "br.com.hyperativa.api.repository"})
@EntityScan(basePackages = {"br.com.hyperativa.card_common.entity", "br.com.hyperativa.api.model.entity"})
public class JpaRepositoryConfig {
}
