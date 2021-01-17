package com.github.shaart.jenkins2discord.notification.auto.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shaart.jenkins2discord.notification.auto.AutoValidator;
import com.github.shaart.jenkins2discord.notification.config.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.dto.discord.FailWebhookInfoDto;
import com.github.shaart.jenkins2discord.notification.dto.discord.WebhookInfoDto;
import com.github.shaart.jenkins2discord.notification.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookValidator implements AutoValidator {

  private final RestTemplate restTemplate;
  private final Jenkins2DiscordProperties properties;

  @Override
  public void validate() throws JsonProcessingException {
    String discordWebhook = properties.getDiscord().getWebhook();
    log.debug("Requesting for Discord Webhook info");
    log.trace("Discord webhook is: {}", discordWebhook);
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(discordWebhook, String.class);

    String jsonBody = responseEntity.getBody();
    ObjectMapper jsonMapper = new ObjectMapper();
    if (responseEntity.getStatusCode() != HttpStatus.OK) {
      log.error("Response from discord server was not OK");
      FailWebhookInfoDto failInfo = jsonMapper.readValue(jsonBody, FailWebhookInfoDto.class);
      String errorMessage = "The Discord server response was not OK: " + failInfo.toString();
      throw new ValidationException(errorMessage);
    }
    WebhookInfoDto webhookInfo = jsonMapper.readValue(jsonBody, WebhookInfoDto.class);
    log.debug("Got response for Discord Webhook info: {}", webhookInfo);
  }
}
