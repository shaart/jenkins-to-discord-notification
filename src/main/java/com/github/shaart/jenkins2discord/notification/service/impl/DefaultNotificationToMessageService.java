package com.github.shaart.jenkins2discord.notification.service.impl;

import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNotificationToMessageService implements NotificationToMessageService {

  private final Jenkins2DiscordProperties properties;

  @Override
  public MessageDto createMessage(JenkinsNotificationDto notification) {
    MessageInfo messageInfo = properties.getDiscord().getMessageInfo();

    String message = toMessageContent(notification);
    String messageContent = messageInfo.getPrefix() + " " + message;

    return MessageDto.builder()
        .username(messageInfo.getUsername())
        .avatarUrl(messageInfo.getAvatarUrl())
        .content(messageContent)
        .build();
  }

  private String toMessageContent(JenkinsNotificationDto notification) {
    try {
      JenkinsBuildDto jobBuild = notification.getJobBuild();
      return String.format(""
          + "Сборка %s (%d): фаза - %s, статус - %s%n"
          + "Ссылка на сборку: %s",
          notification.getJobName(),
          jobBuild.getNumber(),
          jobBuild.getPhase(),
          jobBuild.getStatus(),
          jobBuild.getFullUrl());
    } catch (Exception e) {
      log.error("An error occurred on discord message's content creation", e);
      return "Can't create message because of error: " + e.getMessage();
    }
  }
}
