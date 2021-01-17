package com.github.shaart.jenkins2discord.notification.service.impl;

import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.dto.CommonResponseDto;
import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.service.MessageService;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultMessageService implements MessageService {

  private final RestTemplate restTemplate;
  private final Jenkins2DiscordProperties properties;
  private final NotificationToMessageService notificationToMessageService;

  @Override
  public CommonResponseDto sendMessage(JenkinsNotificationDto notification) {
    String discordWebhookUrl = properties.getDiscord().getWebhook().getUrl();

    MessageDto discordMessage = notificationToMessageService.createMessage(notification);
    try {
      log.debug("Sending a request to Discord's Webhook");
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(discordWebhookUrl, discordMessage, String.class);
      log.debug("Discord's response: {}", responseEntity);
      return CommonResponseDto.createSuccess();
    } catch (RestClientException e) {
      log.error("An error occurred on Discord webhook request", e);
      return CommonResponseDto.createFail();
    }
  }
}
