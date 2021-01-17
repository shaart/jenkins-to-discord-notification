package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JenkinsNotificationDto {

  @JsonProperty("name")
  private String jobName;

  @JsonProperty("url")
  private String jobUrl;

  @JsonProperty("build")
  private JenkinsBuildDto jobBuild;
}
