package com.github.shaart.jenkins2discord.notification.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookInfoDto {

  private int type;
  private String id;
  private String name;
  private String avatar;

  @JsonProperty("channel_id")
  private String channelId;

  @JsonProperty("guild_id")
  private String guildId;

  @JsonProperty("application_id")
  private String applicationId;

  private String token;

}
