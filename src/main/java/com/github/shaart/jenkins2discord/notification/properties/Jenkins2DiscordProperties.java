package com.github.shaart.jenkins2discord.notification.properties;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.shaart.jenkins2discord.notification.dto.PairDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "jenkins2discord")
public class Jenkins2DiscordProperties {

  private Jenkins jenkins;
  private Discord discord;
  private Request request;

  @JsonIgnore
  public static Jenkins2DiscordProperties empty() {
    Jenkins2DiscordProperties jenkins2DiscordProperties = new Jenkins2DiscordProperties();
    Jenkins jenkins = new Jenkins();
    jenkins.setJobFilters(emptyList());

    User user = new User();
    jenkins.setUser(user);

    jenkins2DiscordProperties.setJenkins(jenkins);

    Discord discord = new Discord();

    Webhook webhook = new Webhook();
    discord.setWebhook(webhook);

    MessageInfo message = new MessageInfo();
    discord.setMessage(message);
    jenkins2DiscordProperties.setDiscord(discord);

    Request request = new Request();
    jenkins2DiscordProperties.setRequest(request);

    return jenkins2DiscordProperties;
  }

  @Data
  public static class Jenkins {

    private String address;
    private User user;
    private List<JobFilter> jobFilters;
  }

  @Data
  public static class User {

    private String username;
    private String password;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JobFilter {

    private String name;
    private List<String> displayParameters;
    private List<FilteringParameter> filterByParameters;

    @JsonIgnore
    public boolean matchesJobName(JenkinsNotificationDto notification) {
      return name == null || name.equals(notification.getJobName());
    }

    @JsonIgnore
    public boolean matchesJob(JenkinsNotificationDto notification, JenkinsJobInfo jobInfo) {
      if (!matchesJobName(notification)) {
        return false;
      }

      Map<String, String> parameters = jobInfo.getParameters();
      return parameters.entrySet()
          .stream()
          .map(parameterEntry -> new PairDto<>(
              parameterEntry,
              filterByParameters.stream()
                  .filter(Objects::nonNull)
                  .filter(filteringParameter -> filteringParameter.hasName(parameterEntry.getKey()))
                  .findFirst())
          )
          .filter(pair -> pair.getRight().isPresent())
          .allMatch(pair -> {
            List<String> allowedValues = pair.getRight()
                .get()
                .getAllowedValues();
            if (allowedValues == null || allowedValues.isEmpty()) {
              return true;
            }

            String parameterValue = pair.getLeft().getValue();
            return allowedValues.contains(parameterValue);
          });
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FilteringParameter {

    private String name;
    private List<String> allowedValues;

    public boolean hasName(String parameterName) {
      return name == null || name.equals(parameterName);
    }
  }

  @Data
  public static class Discord {

    private Webhook webhook;
    private MessageInfo message;
  }

  @Data
  public static class Webhook {

    private String url;
  }

  @Data
  public static class MessageInfo {

    private String username;
    private String avatarUrl;
    private String templatePath;
  }

  @Data
  public static class Request {

    private long connectTimeout;
    private long readTimeout;
  }
}
