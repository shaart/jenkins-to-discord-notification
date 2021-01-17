package com.github.shaart.jenkins2discord.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jenkins2discord")
public class Jenkins2DiscordProperties {

  private Jenkins jenkins;
  private Discord discord;
  private Request request;

  @Data
  public static class Jenkins {

    private String address;
  }

  @Data
  public static class Discord {

    private String webhook;
  }

  @Data
  public static class Request {

    private long connectTimeout;
    private long readTimeout;
  }
}
