package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsJobActionInfo {

  @JsonProperty("_class")
  private String entityClass;

  @JsonProperty("parameters")
  private List<JenkinsJobParameterInfo> parameters;

  @JsonProperty("causes")
  private List<JenkinsJobCauseInfo> causes;

  public boolean isParametersAction() {
    return "hudson.model.ParametersAction".equals(entityClass);
  }

  public boolean isCauseAction() {
    return "hudson.model.CauseAction".equals(entityClass);
  }
}
