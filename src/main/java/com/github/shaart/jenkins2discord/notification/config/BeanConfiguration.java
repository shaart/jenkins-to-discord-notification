package com.github.shaart.jenkins2discord.notification.config;

import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

  private final Jenkins2DiscordProperties properties;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    Request propertiesRequest = properties.getRequest();
    Duration connectTimeout = Duration.of(propertiesRequest.getConnectTimeout(), ChronoUnit.MILLIS);
    Duration readTimeout = Duration.of(propertiesRequest.getReadTimeout(), ChronoUnit.MILLIS);
    return restTemplateBuilder
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout)
        .build();
  }
}
