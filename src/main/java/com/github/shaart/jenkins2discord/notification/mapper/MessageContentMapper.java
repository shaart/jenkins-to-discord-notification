package com.github.shaart.jenkins2discord.notification.mapper;

import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_FULL_URL;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_NUMBER;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_PHASE;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.BUILD_STATUS;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_NAME;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_PARAMETERS;
import static com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable.JOB_USER;

import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@NoArgsConstructor
public class MessageContentMapper {

  private static final String EACH_PARAMETER_PREFIX = "-";

  public String toMessageContent(JenkinsNotificationDto notification,
      JenkinsJobInfo jobInfo, String template) {
    try {
      JenkinsBuildDto jobBuild = notification.getJobBuild();

      String parameterMessagePrefix = System.lineSeparator() + " " + EACH_PARAMETER_PREFIX + " ";

      String jobParametersString = jobInfo.getParameters()
          .entrySet()
          .stream()
          .map(entry -> entry.getKey() + ": " + entry.getValue())
          .collect(Collectors.joining(
              parameterMessagePrefix,
              parameterMessagePrefix,
              ""));

      if (parameterMessagePrefix.equals(jobParametersString)) {
        jobParametersString = "";
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
