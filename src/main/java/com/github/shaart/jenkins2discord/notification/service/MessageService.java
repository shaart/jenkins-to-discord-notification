package com.github.shaart.jenkins2discord.notification.service;

import com.github.shaart.jenkins2discord.notification.dto.CommonResponseDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;

public interface MessageService {

  CommonResponseDto sendMessage(JenkinsNotificationDto notification);
}
