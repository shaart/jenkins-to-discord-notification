package com.github.shaart.jenkins2discord.notification.service;

import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;

public interface NotificationToMessageService {

  MessageDto createMessage(JenkinsNotificationDto notification);
}
