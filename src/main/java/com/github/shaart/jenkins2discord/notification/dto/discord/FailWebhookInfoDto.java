package com.github.shaart.jenkins2discord.notification.dto.discord;

import lombok.Data;

@Data
public class FailWebhookInfoDto {

  private String message;
  private String code;
}
