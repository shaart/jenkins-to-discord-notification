package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JenkinsBuildDto {

  @JsonProperty("full_url")
  private String fullUrl;

  private String number;
  private String phase;
  private String status;
  private String url;

  @JsonProperty("scm")
  private JenkinsVersionControlSystemDto versionControlSystem;
}
