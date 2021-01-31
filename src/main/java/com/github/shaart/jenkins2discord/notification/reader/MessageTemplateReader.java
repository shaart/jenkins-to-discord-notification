package com.github.shaart.jenkins2discord.notification.reader;

import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageTemplateReader {

  private final Jenkins2DiscordProperties properties;

  public String readTemplate() {
    String templatePath = properties.getDiscord().getMessage().getTemplatePath();
    try {
      Path path = Paths.get(templatePath);
      return Files.readAllLines(path)
          .stream()
          .collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong on template file read", e);
    }
  }
}
