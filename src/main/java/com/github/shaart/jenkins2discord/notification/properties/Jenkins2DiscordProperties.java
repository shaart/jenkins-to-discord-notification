package com.github.shaart.jenkins2discord.notification.properties;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.shaart.jenkins2discord.notification.dto.PairDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.factory.YamlPropertySourceFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "jenkins2discord")
@PropertySource(value = "classpath:jenkins2discord.yaml", factory = YamlPropertySourceFactory.class)
public class Jenkins2DiscordProperties {

  private Jenkins jenkins = Jenkins.empty();
  private Discord discord = Discord.empty();
  private Request request = Request.empty();

  @JsonIgnore
  public static Jenkins2DiscordProperties empty() {
    return new Jenkins2DiscordProperties();
  }

  @Data
  public static class Jenkins {

    private Boolean checkOnStartup = false;
    private String address;
    private User user = User.empty();
    private List<JobFilter> jobFilters = emptyList();

    public static Jenkins empty() {
      return new Jenkins();
    }
  }

  @Data
  public static class User {

    private String username;
    private String password;

    public static User empty() {
      return new User();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JobFilter {

    private String name;
    @Builder.Default
    private List<String> displayParameters = emptyList();
    @Builder.Default
    private List<FilteringParameter> filterByParameters = emptyList();

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
    @Builder.Default
    private List<String> allowedValues = emptyList();

    public boolean hasName(String parameterName) {
      return name == null || name.equals(parameterName);
    }
  }

  @Data
  public static class Discord {

    private Webhook webhook = Webhook.empty();
    private MessageInfo message = MessageInfo.empty();

    public static Discord empty() {
      return new Discord();
    }
  }

  @Data
  public static class Webhook {

    private String url;

    public static Webhook empty() {
      return new Webhook();
    }
  }

  @Data
  public static class MessageInfo {

    private String username;
    private String avatarUrl;
    private String templatePath;

    public static MessageInfo empty() {
      return new MessageInfo();
    }
  }

  @Data
  public static class Request {

    private long connectTimeout = 5000;
    private long readTimeout = 5000;

    public static Request empty() {
      return new Request();
    }
  }
}
