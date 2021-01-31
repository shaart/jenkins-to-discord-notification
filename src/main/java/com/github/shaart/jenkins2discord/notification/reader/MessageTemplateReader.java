package com.github.shaart.jenkins2discord.notification.reader;

import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateReader {

  private final Jenkins2DiscordProperties properties;

  public String readTemplate() {
    String templatePath = properties.getDiscord().getMessage().getTemplatePath();
    try {
      Path path;
      if (templatePath.startsWith("classpath:")) {
        String pathToFileInClasspath = templatePath.substring("classpath:".length());
        log.debug("Discord's message template path inside jar: '{}'", pathToFileInClasspath);
        return readInsideJar(pathToFileInClasspath);
      }

      log.trace("Discord's message template path: '{}'", templatePath);
      path = Paths.get(templatePath);

      return Files.readAllLines(path)
          .stream()
          .collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong on template file read", e);
    }
  }

  private String readInsideJar(String pathToFileInClasspath) {
    StringJoiner joiner = new StringJoiner(System.lineSeparator());

    try (InputStream inputStream = getResourceAsStream(pathToFileInClasspath);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)
    ) {
      String aLine;
      while ((aLine = reader.readLine()) != null) {
        joiner.add(aLine);
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Resource " + pathToFileInClasspath + " not found in classpath or something went wrong",
          e);
    }

    return joiner.toString();
  }

  private InputStream getResourceAsStream(String resource) {
    ClassLoader contextClassLoader = getContextClassLoader();
    final InputStream fileStream = contextClassLoader.getResourceAsStream(resource);

    if (fileStream == null) {
      return getClass().getResourceAsStream(resource);
    }
    return fileStream;
  }

  private ClassLoader getContextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
