package br.edu.fateczl.tcc.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppProperties.class, AppAuthProperties.class})
public class AppConfig {
}
