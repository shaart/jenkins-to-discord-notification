package com.github.shaart.jenkins2discord.notification.auto.impl;

import com.github.shaart.jenkins2discord.notification.auto.AutoValidator;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class JenkinsValidator implements AutoValidator {

  private final RestTemplate restTemplate;
  private final Jenkins2DiscordProperties properties;

  @Override
  public void validate() {
    String jenkinsAddress = properties.getJenkins().getAddress();
    log.debug("Requesting for Jenkins info");
    log.trace("Jenkins address is: {}", jenkinsAddress);
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(jenkinsAddress, String.class);

    HttpHeaders headers = responseEntity.getHeaders();
    String jenkinsVersion = headers.getFirst("X-Jenkins");
    if (StringUtils.hasText(jenkinsVersion)) {
      log.debug("Got a jenkins version: {}", jenkinsVersion);
      return;
    }
    throw new ValidationException("Jenkins version receiving was failed");
  }
}
