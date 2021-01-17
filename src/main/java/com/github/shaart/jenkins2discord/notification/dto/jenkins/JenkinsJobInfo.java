package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsJobInfo {

  @JsonProperty("actions")
  private List<JenkinsJobActionInfo> actions;

  public static JenkinsJobInfo empty() {
    JenkinsJobInfo jenkinsJobInfo = new JenkinsJobInfo();

    jenkinsJobInfo.setActions(Collections.emptyList());

    return jenkinsJobInfo;
  }

  public Optional<String> getUserId() {
    return actions.stream()
        .filter(JenkinsJobActionInfo::isCauseAction)
        .map(JenkinsJobActionInfo::getCauses)
        .flatMap(Collection::stream)
        .map(JenkinsJobCauseInfo::getUserId)
        .findFirst();
  }

  public Map<String, String> getParameters() {
    return actions.stream()
        .filter(JenkinsJobActionInfo::isParametersAction)
        .map(JenkinsJobActionInfo::getParameters)
        .flatMap(Collection::stream)
        .collect(toMap(JenkinsJobParameterInfo::getName, JenkinsJobParameterInfo::getValue));
  }
}
