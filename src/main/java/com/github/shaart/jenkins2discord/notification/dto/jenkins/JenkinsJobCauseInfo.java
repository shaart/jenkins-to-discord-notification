package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsJobCauseInfo {

  @JsonProperty("userId")
  private String userId;
}
