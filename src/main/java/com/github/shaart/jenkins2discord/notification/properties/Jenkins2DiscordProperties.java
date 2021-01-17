package com.github.shaart.jenkins2discord.notification.properties;

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
    private User user;
  }

  @Data
  public static class User {

    private String username;
    private String password;
  }

  @Data
  public static class Discord {

    private Webhook webhook;
    private MessageInfo message;
  }

  @Data
  public static class Webhook {

    private String url;
  }

  @Data
  public static class MessageInfo {

    private String username;
    private String avatarUrl;
    private String prefix;
  }

  @Data
  public static class Request {
    private long connectTimeout;
    private long readTimeout;
  }
}
