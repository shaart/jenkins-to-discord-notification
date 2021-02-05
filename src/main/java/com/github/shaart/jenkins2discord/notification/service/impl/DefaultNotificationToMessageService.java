package com.github.shaart.jenkins2discord.notification.service.impl;

import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.mapper.MessageContentMapper;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.JobFilter;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.User;
import com.github.shaart.jenkins2discord.notification.reader.MessageTemplateReader;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNotificationToMessageService implements NotificationToMessageService {

  public static final String JENKINS_JOB_API_TREE = "actions[parameters[name,value],causes[userId]]";
  public static final String ERROR_MESSAGE_FORMAT =
      "[ERROR] Internal error occurred for a job '%s'%n%s";

  private final Jenkins2DiscordProperties properties;
  private final RestTemplate restTemplate;
  private final MessageContentMapper messageContentMapper;
  private final MessageTemplateReader messageTemplateReader;

  @Override
  public MessageDto createMessage(JenkinsNotificationDto notification) {
    log.info("Creating a discord message");
    MessageInfo messageInfo = properties.getDiscord().getMessage();
    try {
      List<JobFilter> jobFilters = properties.getJenkins().getJobFilters();
      boolean hasActiveFilters = jobFilters != null && !jobFilters.isEmpty();
      if (hasActiveFilters) {
        boolean hasMatchingFiltersWithName = jobFilters.stream()
            .anyMatch(filter -> filter.matchesJobName(notification));

        if (!hasMatchingFiltersWithName) {
          return MessageDto.createIgnored();
        }
      }

      JenkinsJobInfo jobInfo = retrieveJobParameters(notification);

      if (hasActiveFilters) {
        boolean hasMatchingFilters = jobFilters.stream()
            .anyMatch(filter -> filter.matchesJob(notification, jobInfo));

        if (!hasMatchingFilters) {
          return MessageDto.createIgnored();
        }
      }

      String template = messageTemplateReader.readTemplate();
      String message = messageContentMapper.toMessageContent(notification, jobInfo, template);

      MessageDto messageDto = MessageDto.builder()
          .username(messageInfo.getUsername())
          .avatarUrl(messageInfo.getAvatarUrl())
          .content(message)
          .build();
      log.info("Discord message built");
      return messageDto;
    } catch (Exception e) {
      log.error("An error occurred on message creation", e);
      StringWriter errorWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(errorWriter);
      e.printStackTrace(printWriter);
      String errorStackTrace = errorWriter.toString();
      String messageText = String.format(
          ERROR_MESSAGE_FORMAT,
          notification.getJobName(),
          errorStackTrace);
      return MessageDto.builder()
          .username(messageInfo.getUsername())
          .avatarUrl(messageInfo.getAvatarUrl())
          .content(messageText)
          .build();
    }
  }

  private JenkinsJobInfo retrieveJobParameters(JenkinsNotificationDto notification) {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> request = new HttpEntity<>(headers);

    log.debug("Creating authorization header for request");
    String credentials = createBasicAuthHeaderBase64Value();
    headers.add("Authorization", "Basic " + credentials);

    log.info("Requesting for job build's info");
    String url = notification.getJobBuild().getFullUrl()
        + "/api/json"
        + "?tree=" + JENKINS_JOB_API_TREE;
    ResponseEntity<JenkinsJobInfo> response = restTemplate
        .exchange(url, HttpMethod.GET, request, JenkinsJobInfo.class);
    JenkinsJobInfo jenkinsJobInfo = response.getBody();
    if (jenkinsJobInfo != null) {
      log.info("Job build's info response: {}", jenkinsJobInfo);
      return jenkinsJobInfo;
    }

    log.warn("No body produced for build's info request");
    return JenkinsJobInfo.empty();
  }

  private String createBasicAuthHeaderBase64Value() {
    User jenkinsUser = properties.getJenkins().getUser();
    String concatenatedCredentials = jenkinsUser.getUsername() + ":" + jenkinsUser.getPassword();
    byte[] namePasswordEncoded = Base64.getEncoder().encode(concatenatedCredentials.getBytes());
    return new String(namePasswordEncoded);
  }

}
