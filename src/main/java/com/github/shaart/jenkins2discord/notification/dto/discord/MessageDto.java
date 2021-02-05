package com.github.shaart.jenkins2discord.notification.dto.discord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {

  @JsonInclude(Include.NON_NULL)
  private String username;

  @JsonInclude(Include.NON_NULL)
  private String avatarUrl;

  private String content;

  @JsonIgnore
  @Builder.Default
  private boolean isIgnored = false;

  public static MessageDto createIgnored() {
    return MessageDto.builder()
        .content(null)
        .isIgnored(true)
        .build();
  }
}
