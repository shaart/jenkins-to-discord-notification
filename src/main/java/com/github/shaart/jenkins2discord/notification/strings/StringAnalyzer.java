package com.github.shaart.jenkins2discord.notification.strings;

import com.github.shaart.jenkins2discord.notification.service.impl.DefaultMessageService;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@NoArgsConstructor
public class StringAnalyzer {

  public static final String DISCORD_PREFORMATTED_TEXT = "```";

  public List<String> splitToMessages(String input) {
    if (input == null) {
      input = "";
    }
    log.info("Initial message has length = {}", input.length());

    StringBuilder remainedContent = new StringBuilder(input);
    if (remainedContent.length() <= DefaultMessageService.DISCORD_MAX_MESSAGE_LENGTH) {
      return Collections.singletonList(input);
    }

    List<String> messages = new ArrayList<>();
    while (remainedContent.length() > DefaultMessageService.DISCORD_MAX_MESSAGE_LENGTH) {
      String messagePart = remainedContent
          .substring(0, DefaultMessageService.DISCORD_MAX_MESSAGE_LENGTH);
      remainedContent.replace(0, remainedContent.length(),
          remainedContent.substring(DefaultMessageService.DISCORD_MAX_MESSAGE_LENGTH));

      int unclosedPreformattingIndex = getUnclosedPreformattingIndex(messagePart);
      if (unclosedPreformattingIndex > 0) {
        String addToNextPart = messagePart.substring(unclosedPreformattingIndex);
        messagePart = messagePart.substring(0, unclosedPreformattingIndex);
        remainedContent.insert(0, addToNextPart);
      }
      boolean isAtStart = unclosedPreformattingIndex == 0;
      if (isAtStart) {
        int endIndex = messagePart.length() - DISCORD_PREFORMATTED_TEXT.length();
        String addToNextPartWithPreformatted = messagePart.substring(endIndex);
        messagePart = messagePart.substring(0, endIndex) + DISCORD_PREFORMATTED_TEXT;

        remainedContent.insert(0, addToNextPartWithPreformatted);
        remainedContent.insert(0, DISCORD_PREFORMATTED_TEXT);
      }

      messages.add(messagePart);

      log.info("Split into messages with length {} and {}",
          messagePart.length(),
          remainedContent.length());
    }
    messages.add(remainedContent.toString());

    return messages;
  }

  public int getUnclosedPreformattingIndex(String input) {
    Pattern pattern = Pattern.compile(DISCORD_PREFORMATTED_TEXT);
    Matcher matcher = pattern.matcher(input);
    int matchesCount = 0;
    while (matcher.find()) {
      matchesCount++;
    }
    boolean isOddMatchesCount = matchesCount % 2 == 1;
    if (isOddMatchesCount) {
      return input.lastIndexOf(DISCORD_PREFORMATTED_TEXT);
    }
    return -1;
  }

  public @NonNull String getContentLengthOrUnknown(String aMessage) {
    return Optional.of(aMessage)
        .map(String::length)
        .map(String::valueOf)
        .orElse("unknown");
  }
}