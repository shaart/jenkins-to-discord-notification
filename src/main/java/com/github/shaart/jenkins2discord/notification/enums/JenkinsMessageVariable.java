package com.github.shaart.jenkins2discord.notification.enums;

import com.github.shaart.jenkins2discord.notification.constant.MessageConstants;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobActionInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;

@Getter
@RequiredArgsConstructor
public enum JenkinsMessageVariable {
  JOB_NAME("JOB_NAME",
      JenkinsNotificationDto::setJobName,
      (jobInfo, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_JOB_INFO_SET);
      }),

  BUILD_NUMBER("BUILD_NUMBER",
      (notification, value) -> {
        JenkinsBuildDto jobBuild = notification.getJobBuild();
        jobBuild.setNumber(value);
      },
      (jobInfo, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_JOB_INFO_SET);
      }),

  BUILD_PHASE("BUILD_PHASE",
      (notification, value) -> {
        JenkinsBuildDto jobBuild = notification.getJobBuild();
        jobBuild.setPhase(value);
      },
      (jobInfo, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_JOB_INFO_SET);
      }),

  BUILD_STATUS("BUILD_STATUS",
      (notification, value) -> {
        JenkinsBuildDto jobBuild = notification.getJobBuild();
        jobBuild.setStatus(value);
      },
      (jobInfo, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_JOB_INFO_SET);
      }),

  BUILD_FULL_URL("BUILD_FULL_URL",
      (notification, value) -> {
        JenkinsBuildDto jobBuild = notification.getJobBuild();
        jobBuild.setFullUrl(value);
      },
      (jobInfo, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_JOB_INFO_SET);
      }),

  JOB_USER("JOB_USER",
      (notification, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_BUILD_INFO_SET);
      },
      JenkinsJobInfo::addUser),

  JOB_PARAMETERS("JOB_PARAMETERS",
      (notification, value) -> {
        throw new UnsupportedOperationException(MessageConstants.UNSUPPORTED_BUILD_INFO_SET);
      },
      (jobInfo, value) -> {
        String[] keyValue = value.split("=");
        String paramKey = keyValue[0];
        String paramValue = keyValue[1];

        JenkinsJobActionInfo parameterAction = jobInfo.getOrCreateParameterAction();
        parameterAction.addParameter(paramKey, paramValue);
      });

  public static final String TEMPLATE_FORMAT = "${%s}";

  final String name;
  final BiConsumer<JenkinsNotificationDto, String> setBuildValueFunction;
  final BiConsumer<JenkinsJobInfo, String> setJobValueFunction;

  public String getMessageTemplate() {
    return String.format(TEMPLATE_FORMAT, getName());
  }

  public void setValue(JenkinsNotificationDto notification, String value) {
    getSetBuildValueFunction().accept(notification, value);
  }

  public void setValue(JenkinsJobInfo jobInfo, String value) {
    getSetJobValueFunction().accept(jobInfo, value);
  }

}
