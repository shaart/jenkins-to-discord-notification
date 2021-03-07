package com.github.shaart.jenkins2discord.notification.service.impl;

import com.github.shaart.jenkins2discord.notification.dto.CommonResponseDto;
import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.service.MessageService;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import com.github.shaart.jenkins2discord.notification.strings.StringAnalyzer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultMessageService implements MessageService {

  public static final int DISCORD_MAX_MESSAGE_LENGTH = 2000;

  private final RestTemplate restTemplate;
  private final Jenkins2DiscordProperties properties;
  private final NotificationToMessageService notificationToMessageService;
  private final StringAnalyzer stringAnalyzer;

  @Override
  public CommonResponseDto sendMessage(JenkinsNotificationDto notification) {
    String discordWebhookUrl = properties.getDiscord().getWebhook().getUrl();

    MessageDto discordMessage = notificationToMessageService.createMessage(notification);
    if (discordMessage.isIgnored()) {
      log.info("Ignored message for notification: {}", discordMessage);
      return CommonResponseDto.createSuccess();
    }
    return sendMessageToDiscord(discordWebhookUrl, discordMessage);
  }

  private CommonResponseDto sendMessageToDiscord(String discordWebhookUrl,
      @NonNull MessageDto discordMessage) {
    try {
      log.info("Sending a request to Discord's Webhook");
      log.trace("Webhook url: {}", discordWebhookUrl);
      String initialContent = discordMessage.getContent();
      List<String> messages = stringAnalyzer.splitToMessages(initialContent);
      messages.forEach(aMessage -> {
        MessageDto messageToSend = MessageDto.createFrom(discordMessage, aMessage);
        String messageToSendContent = messageToSend.getContent();
        log.info("Sending message part with length = {}",
            stringAnalyzer.getContentLengthOrUnknown(messageToSendContent));
        log.trace("Message content: {}", messageToSendContent);

        ResponseEntity<String> responseEntity =
            restTemplate.postForEntity(discordWebhookUrl, messageToSend, String.class);
        log.debug("Discord's response: {}", responseEntity);
      });

      return CommonResponseDto.createSuccess();
    } catch (RestClientException e) {
      log.error("An error occurred on Discord webhook request", e);
      return CommonResponseDto.createFail();
    }
  }
}
