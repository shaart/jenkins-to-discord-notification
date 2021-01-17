package com.github.shaart.jenkins2discord.notification.controller;

import com.github.shaart.jenkins2discord.notification.dto.CommonResponseDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  public CommonResponseDto sendMessage(JenkinsNotificationDto notification) {
    return messageService.sendMessage(notification);
  }
}
