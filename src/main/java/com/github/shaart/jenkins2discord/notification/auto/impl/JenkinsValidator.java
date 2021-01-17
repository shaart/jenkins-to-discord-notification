package com.github.shaart.jenkins2discord.notification.auto.impl;

import com.github.shaart.jenkins2discord.notification.auto.AutoValidator;
import com.github.shaart.jenkins2discord.notification.exception.ValidationException;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class JenkinsValidator implements AutoValidator {

  private final RestTemplate restTemplate;
  private final Jenkins2DiscordProperties properties;

  public JenkinsValidator(@Qualifier("noFailRestTemplate") RestTemplate restTemplate,
      Jenkins2DiscordProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  @Override
  public void validate() {
    boolean isNeededJenkinsConnection = false;
    log.info("Is needed Jenkins connection = {}", isNeededJenkinsConnection);
    if (isNeededJenkinsConnection) {
      String jenkinsAddress = properties.getJenkins().getAddress();
      log.debug("Requesting for Jenkins info");
      log.trace("Jenkins address is: {}", jenkinsAddress);
      try {
        ResponseEntity<String> responseEntity = restTemplate
            .getForEntity(jenkinsAddress, String.class);

        HttpHeaders headers = responseEntity.getHeaders();
        String jenkinsVersion = headers.getFirst("X-Jenkins");
        if (StringUtils.hasText(jenkinsVersion)) {
          log.debug("Got a jenkins version: {}", jenkinsVersion);
          return;
        }
      } catch (RestClientException e) {
        log.error(e.getMessage(), e);
        throw new ValidationException("Jenkins version receiving was failed");
      }
      throw new ValidationException("Jenkins version receiving was failed");
    }
  }
}
