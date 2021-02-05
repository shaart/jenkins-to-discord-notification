package com.github.shaart.jenkins2discord.notification.mapper;

import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_FULL_URL;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_NUMBER;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_PHASE;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_STATUS;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_NAME;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_PARAMETERS;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_USER;
import static java.util.Collections.emptyList;

import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.JobFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageContentMapper {

  private static final String EACH_PARAMETER_PREFIX = "-";

  private final Jenkins2DiscordProperties properties;

  public String toMessageContent(JenkinsNotificationDto notification,
      JenkinsJobInfo jobInfo, String template) {
    try {
      JenkinsBuildDto jobBuild = notification.getJobBuild();

      String parameterMessagePrefix = System.lineSeparator() + " " + EACH_PARAMETER_PREFIX + " ";

      List<JobFilter> jobFilters = properties.getJenkins().getJobFilters();

      List<String> displayParameters = emptyList();
      if (jobFilters != null && !jobFilters.isEmpty()) {
        JobFilter matchingFilter = jobFilters.stream()
            .filter(filter -> filter.matchesJobName(notification))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Not matched job pass filters"));
        displayParameters = matchingFilter.getDisplayParameters();
      }
      final List<String> parametersToDisplay = displayParameters;
      String jobParametersString = jobInfo.getParameters()
          .entrySet()
          .stream()
          .filter(entry ->
              parametersToDisplay.isEmpty() || parametersToDisplay.contains(entry.getKey()))
          .map(entry -> entry.getKey() + ": " + entry.getValue())
          .collect(Collectors.joining(
              parameterMessagePrefix,
              parameterMessagePrefix,
              ""));

      if (parameterMessagePrefix.equals(jobParametersString)) {
        jobParametersString = "";
      }

      if (jobParametersString.isEmpty()) {
        jobParametersString = "-";
      }

      return template
          .replace(JOB_NAME.getMessageTemplate(), getOrEmpty(notification.getJobName()))
          .replace(BUILD_NUMBER.getMessageTemplate(), getOrEmpty(jobBuild.getNumber()))
          .replace(BUILD_PHASE.getMessageTemplate(), getOrEmpty(jobBuild.getPhase()))
          .replace(BUILD_STATUS.getMessageTemplate(), getOrEmpty(jobBuild.getStatus()))
          .replace(BUILD_FULL_URL.getMessageTemplate(), getOrEmpty(jobBuild.getFullUrl()))
          .replace(JOB_USER.getMessageTemplate(), getOrEmpty(jobInfo.getUserId().orElse(null)))
          .replace(JOB_PARAMETERS.getMessageTemplate(), getOrEmpty(jobParametersString));
    } catch (Exception e) {
      log.error("An error occurred on discord message's content creation", e);
      return "Can't create message because of error: " + e.getMessage();
    }
  }

  private String getOrEmpty(String value) {
    if (value == null) {
      return "";
    }
    return value;
  }
}
