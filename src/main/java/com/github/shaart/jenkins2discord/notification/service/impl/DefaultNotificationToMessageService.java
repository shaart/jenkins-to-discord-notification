package com.github.shaart.jenkins2discord.notification.service.impl;

import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.User;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNotificationToMessageService implements NotificationToMessageService {

  public static final String JENKINS_JOB_API_TREE = "actions[parameters[name,value],causes[userId]]";
  private static final String EACH_PARAMETER_PREFIX = "-";
  private final Jenkins2DiscordProperties properties;
  private final RestTemplate restTemplate;

  @Override
  public MessageDto createMessage(JenkinsNotificationDto notification) {
    log.info("Creating a discord message");
    MessageInfo messageInfo = properties.getDiscord().getMessage();

    JenkinsJobInfo jenkinsJobInfo = retrieveJobParameters(notification);
    String message = toMessageContent(notification, jenkinsJobInfo);
    String messageContent = messageInfo.getPrefix() + " " + message;

    MessageDto messageDto = MessageDto.builder()
        .username(messageInfo.getUsername())
        .avatarUrl(messageInfo.getAvatarUrl())
        .content(messageContent)
        .build();
    log.info("Discord message built");
    return messageDto;
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

  private String toMessageContent(JenkinsNotificationDto notification,
      JenkinsJobInfo jenkinsJobInfo) {
    try {
      JenkinsBuildDto jobBuild = notification.getJobBuild();

      String jobParametersString = jenkinsJobInfo.getParameters()
          .entrySet()
          .stream()
          .map(entry -> entry.getKey() + ": " + entry.getValue())
          .collect(Collectors.joining(
              "\n " + EACH_PARAMETER_PREFIX + " ",
              "\n " + EACH_PARAMETER_PREFIX + " ",
              ""));

      return String.format(""
              + "Сборка %s (#%s): фаза - %s, статус - %s%n"
              + "Ссылка на сборку: %s%n"
              + "Кто запустил: %s%n"
              + "Параметры сборки: %s",
          getOrEmpty(notification.getJobName()),
          getOrEmpty(jobBuild.getNumber()),
          getOrEmpty(jobBuild.getPhase()),
          getOrEmpty(jobBuild.getStatus()),
          getOrEmpty(jobBuild.getFullUrl()),
          getOrEmpty(jenkinsJobInfo.getUserId().orElse(null)),
          getOrEmpty(jobParametersString)
      );
    } catch (Exception e) {
      log.error("An error occurred on discord message's content creation", e);
      return "Can't create message because of error: " + e.getMessage();
    }
  }

  private Object getOrEmpty(Object value) {
    if (value == null) {
      return "";
    }
    return value;
  }
}
