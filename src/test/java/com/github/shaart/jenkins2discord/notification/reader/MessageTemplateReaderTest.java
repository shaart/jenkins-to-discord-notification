package com.github.shaart.jenkins2discord.notification.reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Discord;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class MessageTemplateReaderTest {

  public static final List<String> TEMP_FILE_TEST_TEMPLATE_LINES = Arrays.asList(
      "[TEST] Ссылка на сборку: ${BUILD_FULL_URL}",
      "Параметры сборки: ${JOB_PARAMETERS}");
  public static final String TEST_TEMPLATE_PATH = "templates/test_discord_message.template";

  private Jenkins2DiscordProperties properties;

  private MessageTemplateReader messageTemplateReader;

  @BeforeEach
  void setUp() {
    properties = new Jenkins2DiscordProperties();

    Discord discordProperties = new Discord();
    properties.setDiscord(discordProperties);

    MessageInfo messageInfo = new MessageInfo();
    discordProperties.setMessage(messageInfo);

    String testTemplatePath = Paths.get("src",
        "test",
        "resources",
        "templates",
        "test_discord_message.template")
        .toString();
    messageInfo.setTemplatePath(testTemplatePath);

    messageTemplateReader = new MessageTemplateReader(properties);
  }

  @Test
  @DisplayName("readTemplate has test prefix for tests")
  void readTemplateHasTestPrefix() {
    String template = messageTemplateReader.readTemplate();
    assertThat(template, containsString("[TEST]"));
  }

  @Test
  @DisplayName("readTemplate does not throw an exception")
  void readTemplateHasNoException() {
    Assertions.assertDoesNotThrow(() -> {
      messageTemplateReader.readTemplate();
    }, "Should read default template without errors");
  }

  @Test
  @DisplayName("readTemplate can read external file")
  void readTemplateCouldReadExternalFile() {
    withTemporaryFile(file -> {
      String template = messageTemplateReader.readTemplate();
      final String expected = TEMP_FILE_TEST_TEMPLATE_LINES.stream()
          .collect(Collectors.joining(System.lineSeparator()));
      assertThat(template, equalTo(expected));
    });
  }

  @Test
  @DisplayName("readTemplate could read classpath file")
  void readTemplateCouldReadClasspathFile() {
    ClassLoader classLoader = getClass().getClassLoader();
    URL testTemplateResource = classLoader.getResource(TEST_TEMPLATE_PATH);
    String testTemplatePath = Objects.requireNonNull(testTemplateResource).getFile();
    File testTemplate = new File(testTemplatePath);
    properties.getDiscord()
        .getMessage()
        .setTemplatePath(testTemplate.getPath());

    String template = messageTemplateReader.readTemplate();

    assertThat(template, containsString("[TEST]"));
  }

  private void withTemporaryFile(Consumer<Path> fileConsumer) {
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("discord_message_template", ".template");
      Files.write(tempFile, TEMP_FILE_TEST_TEMPLATE_LINES);

      properties.getDiscord()
          .getMessage()
          .setTemplatePath(tempFile.toAbsolutePath().toString());

      fileConsumer.accept(tempFile);

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      tryDeleteFile(tempFile);
    }
  }

  private void tryDeleteFile(Path tempFile) {
    if (tempFile != null) {
      try {
        Files.delete(tempFile);
      } catch (IOException e) {
        System.err.println("Can't delete temp file '" + tempFile + "'");
      }
    }
  }
}