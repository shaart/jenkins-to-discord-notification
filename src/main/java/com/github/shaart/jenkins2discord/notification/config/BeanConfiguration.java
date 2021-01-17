package com.github.shaart.jenkins2discord.notification.config;

import com.github.shaart.jenkins2discord.notification.handler.resttemplate.NoExceptionErrorHandler;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

  private final Jenkins2DiscordProperties properties;

  @Bean
  @Primary
  public RestTemplate defaultRestTemplate(RestTemplateBuilder restTemplateBuilder,
      ResponseErrorHandler responseErrorHandler) {
    Request propertiesRequest = properties.getRequest();
    Duration connectTimeout = Duration.of(propertiesRequest.getConnectTimeout(), ChronoUnit.MILLIS);
    Duration readTimeout = Duration.of(propertiesRequest.getReadTimeout(), ChronoUnit.MILLIS);
    return restTemplateBuilder
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout)
        .build();
  }

  @Bean
  public RestTemplate noFailRestTemplate(RestTemplateBuilder restTemplateBuilder,
      @Qualifier("noFailResponseErrorHandler") ResponseErrorHandler responseErrorHandler) {
    Request propertiesRequest = properties.getRequest();
    Duration connectTimeout = Duration.of(propertiesRequest.getConnectTimeout(), ChronoUnit.MILLIS);
    Duration readTimeout = Duration.of(propertiesRequest.getReadTimeout(), ChronoUnit.MILLIS);
    return restTemplateBuilder
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout)
        .errorHandler(responseErrorHandler)
        .build();
  }

  @Bean
  public ResponseErrorHandler noFailResponseErrorHandler() {
    return new NoExceptionErrorHandler();
  }
}
