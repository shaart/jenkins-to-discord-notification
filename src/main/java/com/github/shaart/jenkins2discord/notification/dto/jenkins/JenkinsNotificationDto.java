package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsNotificationDto {

  @JsonProperty("name")
  private String jobName;

  @JsonProperty("url")
  private String jobUrl;

  @JsonProperty("build")
  private JenkinsBuildDto jobBuild;

  @JsonIgnore
  public JenkinsBuildDto getJobBuild() {
    if (jobBuild == null) {
      jobBuild = new JenkinsBuildDto();
    }
    return jobBuild;
  }
}
