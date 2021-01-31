package com.github.shaart.jenkins2discord.notification.auto.impl;

import com.github.shaart.jenkins2discord.notification.auto.AutoValidator;
import com.github.shaart.jenkins2discord.notification.reader.MessageTemplateReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
public class DiscordMessageTemplateValidator implements AutoValidator {

  private final MessageTemplateReader messageTemplateReader;

  @Override
  public void validate() {
    String template = messageTemplateReader.readTemplate();
    if (ObjectUtils.isEmpty(template)) {
      throw new IllegalStateException("Discord message template is empty");
    }
  }
}
