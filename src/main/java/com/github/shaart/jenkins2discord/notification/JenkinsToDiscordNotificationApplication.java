package com.github.shaart.jenkins2discord.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JenkinsToDiscordNotificationApplication {

  public static void main(String[] args) {
    SpringApplication.run(JenkinsToDiscordNotificationApplication.class, args);
  }

}
