package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

  public static JenkinsJobActionInfo createEmptyParametersAction() {
    return new JenkinsJobActionInfo("hudson.model.ParametersAction",
        new ArrayList<>(),
        null);
  }

  public static JenkinsJobActionInfo createEmptyCauseAction() {
    return new JenkinsJobActionInfo("hudson.model.CauseAction",
        null,
        new ArrayList<>());
  }

  @JsonIgnore
  public boolean isParametersAction() {
    return "hudson.model.ParametersAction".equals(entityClass);
  }

  @JsonIgnore
  public boolean isCauseAction() {
    return "hudson.model.CauseAction".equals(entityClass);
  }

  public void addParameter(String key, String value) {
    if (!isParametersAction()) {
      throw new IllegalStateException("Not parameter action, could not add new parameter");
    }
    JenkinsJobParameterInfo parameter = new JenkinsJobParameterInfo(key, value);
    parameters.add(parameter);
  }
}
