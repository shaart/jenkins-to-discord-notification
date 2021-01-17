package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JenkinsNotificationDto {

  private String jobName;

  @JsonProperty("url")
  private String jobUrl;

  private JenkinsBuildDto jobBuild;
}
