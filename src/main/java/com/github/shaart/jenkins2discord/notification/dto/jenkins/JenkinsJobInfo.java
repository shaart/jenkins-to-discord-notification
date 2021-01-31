package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

  @JsonIgnore
  public Optional<String> getUserId() {
    return actions.stream()
        .filter(JenkinsJobActionInfo::isCauseAction)
        .map(JenkinsJobActionInfo::getCauses)
        .flatMap(Collection::stream)
        .map(JenkinsJobCauseInfo::getUserId)
        .findFirst();
  }

  @JsonIgnore
  public Map<String, String> getParameters() {
    return actions.stream()
        .filter(JenkinsJobActionInfo::isParametersAction)
        .map(JenkinsJobActionInfo::getParameters)
        .flatMap(Collection::stream)
        .collect(toMap(JenkinsJobParameterInfo::getName, JenkinsJobParameterInfo::getValue));
  }

  @JsonIgnore
  public List<JenkinsJobActionInfo> getActions() {
    if (actions == null) {
      actions = new ArrayList<>();
    }
    return actions;
  }

  @JsonIgnore
  public JenkinsJobActionInfo getOrCreateParameterAction() {
    List<JenkinsJobActionInfo> localActions = getActions();

    Optional<JenkinsJobActionInfo> seekParameterAction = localActions.stream()
        .filter(JenkinsJobActionInfo::isParametersAction)
        .findFirst();

    if (seekParameterAction.isPresent()) {
      return seekParameterAction.get();
    }

    JenkinsJobActionInfo parameterAction = JenkinsJobActionInfo.createEmptyParametersAction();
    localActions.add(parameterAction);

    return parameterAction;
  }

  public void addUser(String userId) {
    JenkinsJobActionInfo action = JenkinsJobActionInfo.createEmptyCauseAction();
    List<JenkinsJobCauseInfo> causes = action.getCauses();

    JenkinsJobCauseInfo user = new JenkinsJobCauseInfo(userId);
    causes.add(user);

    List<JenkinsJobActionInfo> localActions = getActions();
    localActions.add(action);
  }
}
