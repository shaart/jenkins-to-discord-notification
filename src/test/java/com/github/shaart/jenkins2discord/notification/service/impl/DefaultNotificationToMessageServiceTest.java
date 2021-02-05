package com.github.shaart.jenkins2discord.notification.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.enums.JenkinsMessageVariable;
import com.github.shaart.jenkins2discord.notification.mapper.MessageContentMapper;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Discord;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.FilteringParameter;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Jenkins;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.JobFilter;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.User;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Webhook;
import com.github.shaart.jenkins2discord.notification.reader.MessageTemplateReader;
import com.github.shaart.jenkins2discord.notification.test.WireMockDiscord;
import com.github.shaart.jenkins2discord.notification.test.WireMockJenkins;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class DefaultNotificationToMessageServiceTest {

  public static final String DISCORD_AVATAR_URL =
      "https://image.flaticon.com/icons/png/512/147/147144.png";
  public static final String DISCORD_USERNAME = "common.developer";
  public static final String A_JOB_NAME = "myJob";
  public static final String NOT_A_JOB = "notMyJob";
  public static final String URL_JOB_PATH = "/job/" + A_JOB_NAME + "/5";
  public static final String JENKINS_JOB_URL = WireMockJenkins.HOST_URL + URL_JOB_PATH;
  public static final List<String> EXPECTED_LINES = asList(
      "[TEST] Сборка myJob (#5): фаза - STARTED, статус - SUCCESS",
      "Ссылка на сборку: " + JENKINS_JOB_URL,
      "Кто запустил: user.name",
      "Параметры сборки: ",
      " - NAMESPACE: dev",
      " - GIT_PROJECT: j2d-notification",
      " - GIT_COMMIT: fceadf71",
      " - GIT_BRANCH: develop"
  );

  private Jenkins2DiscordProperties properties;
  private RestTemplate restTemplate;
  private MessageContentMapper messageContentMapper;
  private MessageTemplateReader messageTemplateReader;
  private DefaultNotificationToMessageService service;
  private Jenkins jenkinsProperties;


  @BeforeEach
  public void setUp() {
    properties = Mockito.spy(new Jenkins2DiscordProperties());
    restTemplate = Mockito.spy(new RestTemplate());
    messageContentMapper = Mockito.spy(new MessageContentMapper(properties));
    messageTemplateReader = Mockito.spy(new MessageTemplateReader(properties));

    service = new DefaultNotificationToMessageService(properties, restTemplate,
        messageContentMapper, messageTemplateReader);

    Discord discordProperties = new Discord();
    MessageInfo messageInfo = new MessageInfo();
    messageInfo.setUsername(DISCORD_USERNAME);
    messageInfo.setAvatarUrl(DISCORD_AVATAR_URL);
    String testTemplatePath = Paths.get("src",
        "test",
        "resources",
        "templates",
        "test_discord_message.template")
        .toString();
    messageInfo.setTemplatePath(testTemplatePath);
    discordProperties.setMessage(messageInfo);

    Webhook webhookProperties = new Webhook();
    webhookProperties.setUrl(WireMockDiscord.HOST_URL + WireMockDiscord.WEB_HOOK_URI);
    discordProperties.setWebhook(webhookProperties);

    jenkinsProperties = new Jenkins();
    User user = new User();
    user.setUsername("admin");
    user.setPassword("admin");
    jenkinsProperties.setUser(user);
    jenkinsProperties.setAddress(WireMockJenkins.HOST_URL);

    when(properties.getDiscord())
        .thenReturn(discordProperties);
    when(properties.getJenkins())
        .thenReturn(jenkinsProperties);

    WireMockDiscord.init();
    WireMockJenkins.init();
  }

  @AfterEach
  public void tearDown() {
    WireMockDiscord.stop();
    WireMockDiscord.reset();

    WireMockJenkins.stop();
    WireMockJenkins.reset();
  }

  @Test
  @DisplayName("Everything is ok, no filters or display parameters - all displayed")
  void createMessageNoFilters() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(emptyList())
            .filterByParameters(emptyList())
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .collect(Collectors.joining(System.lineSeparator()));
          String reason = "Everything is ok, no filters or display parameters - all displayed";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has not allowed name, should be ignored")
  void createMessageNotAllowedJobName() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(NOT_A_JOB)
            .displayParameters(emptyList())
            .filterByParameters(emptyList())
            .build()),
        (MessageDto message) -> {
          String reason = "Job has not allowed name, should be ignored";
          assertThat(reason, message.getContent(), is(nullValue()));
          assertThat(reason, message.isIgnored(), is(true));
        }
    );
  }

  @Test
  @DisplayName("Job has two display parameters, other should be ignored")
  void createMessageTwoDisplayParams() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(emptyList())
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_PROJECT")
                      || line.contains("GIT_BRANCH"))
              .collect(Collectors.joining(System.lineSeparator()));
          String reason = "Job has two display parameters, other should be ignored";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has two display parameters without filtering values, filter match - all")
  void createMessageTwoDisplayNoFilteringValues() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(asList(
                FilteringParameter.builder()
                    .name("GIT_PROJECT")
                    .allowedValues(emptyList())
                    .build(),
                FilteringParameter.builder()
                    .name("GIT_BRANCH")
                    .allowedValues(emptyList())
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_PROJECT")
                      || line.contains("GIT_BRANCH"))
              .collect(Collectors.joining(System.lineSeparator()));
          String reason =
              "Job has two display parameters without filtering values, filter match - all";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has two display parameters with filtering values, filter matches - ok")
  void createMessageTwoDisplayFilteringIsOk() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(asList(
                FilteringParameter.builder()
                    .name("GIT_PROJECT")
                    .allowedValues(singletonList("j2d-notification"))
                    .build(),
                FilteringParameter.builder()
                    .name("GIT_BRANCH")
                    .allowedValues(singletonList("develop"))
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_PROJECT")
                      || line.contains("GIT_BRANCH"))
              .collect(Collectors.joining(System.lineSeparator()));
          String reason =
              "Job has two display parameters with filtering values, filter matches - ok";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job filter without name has two display parameters with several filtering "
      + "values, filter matches - ok")
  void createMessageJobNoNameTwoDisplayAndMatchingFilters() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=story/TASK-1", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(null)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(asList(
                FilteringParameter.builder()
                    .name("GIT_PROJECT")
                    .allowedValues(asList("j2d-notification", "other"))
                    .build(),
                FilteringParameter.builder()
                    .name("GIT_BRANCH")
                    .allowedValues(asList("develop", "story/TASK-1"))
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_PROJECT")
                      || line.contains("GIT_BRANCH"))
              .map(line -> {
                if (line.contains("GIT_BRANCH")) {
                  line = " - GIT_BRANCH: story/TASK-1";
                }
                return line;
              })
              .collect(Collectors.joining(System.lineSeparator()));
          String reason =
              "Job filter without name has two display parameters with several filtering "
                  + "values, filter matches - ok";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has two display parameters with several filtering values, filter matches - ok")
  void createMessageTwoDisplayFiltersWithSeveralValuesOneMatching()
      throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=story/TASK-1", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(asList(
                FilteringParameter.builder()
                    .name("GIT_PROJECT")
                    .allowedValues(asList("j2d-notification", "other"))
                    .build(),
                FilteringParameter.builder()
                    .name("GIT_BRANCH")
                    .allowedValues(asList("develop", "story/TASK-1"))
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_PROJECT")
                      || line.contains("GIT_BRANCH"))
              .map(line -> {
                if (line.contains("GIT_BRANCH")) {
                  line = " - GIT_BRANCH: story/TASK-1";
                }
                return line;
              })
              .collect(Collectors.joining(System.lineSeparator()));
          String reason =
              "Job has two display parameters with several filtering values, filter matches - ok";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has two display parameters with filtering values, filter not matches - ignore")
  void createMessageTwoDisplayNotMatchingFilter() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=story/TASK-1", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(asList("GIT_PROJECT", "GIT_BRANCH"))
            .filterByParameters(asList(
                FilteringParameter.builder()
                    .name("GIT_PROJECT")
                    .allowedValues(singletonList("j2d-notification"))
                    .build(),
                FilteringParameter.builder()
                    .name("GIT_BRANCH")
                    .allowedValues(singletonList("develop"))
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String reason =
              "Job has two display parameters with filtering values, filter not matches - ignore";
          assertThat(reason, message.getContent(), is(nullValue()));
          assertThat(reason, message.isIgnored(), is(true));
        }
    );
  }

  @Test
  @DisplayName("Job has one display parameter, other should be ignored")
  void createMessageOneDisplayParameter() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(singletonList("GIT_BRANCH"))
            .filterByParameters(emptyList())
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_BRANCH"))
              .collect(Collectors.joining(System.lineSeparator()));
          String reason = "Job has one display parameter, other should be ignored";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has one display parameter with another filtering all matches")
  void createMessageOneDisplayButAnotherFilteringMatch() throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(singletonList("GIT_BRANCH"))
            .filterByParameters(singletonList(
                FilteringParameter.builder()
                    .name("NAMESPACE")
                    .allowedValues(singletonList("dev"))
                    .build()
            ))
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки")
                      || line.contains("GIT_BRANCH"))
              .collect(Collectors.joining(System.lineSeparator()));
          String reason = "Job has one display parameter with another filtering all matches";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  @DisplayName("Job has no SEEKING display parameter -> message with no build parameters")
  void createMessageNoDisplayParameterInJobCausesAbsentParametersSection()
      throws JsonProcessingException {
    createMessageWithActiveFilters(
        asList("GIT_PROJECT=j2d-notification", "GIT_BRANCH=develop", "GIT_COMMIT=fceadf71",
            "NAMESPACE=dev"),
        singletonList(JobFilter.builder()
            .name(A_JOB_NAME)
            .displayParameters(singletonList("GIT_MERGE_REQUEST"))
            .filterByParameters(emptyList())
            .build()),
        (MessageDto message) -> {
          String expectedMessage = EXPECTED_LINES.stream()
              .filter(line ->
                  line.contains(A_JOB_NAME)
                      || line.contains(JENKINS_JOB_URL)
                      || line.contains("user.name")
                      || line.contains("Параметры сборки"))
              .map(line -> {
                if (line.contains("Параметры сборки")) {
                  line = "Параметры сборки: -";
                }
                return line;
              })
              .collect(Collectors.joining(System.lineSeparator()));
          String reason = "Job has no specified display parameters - has no build parameters";
          assertThat(reason, message.getContent(), is(expectedMessage));
          assertThat(reason, message.isIgnored(), is(false));
        }
    );
  }

  @Test
  void createMessage() throws JsonProcessingException {
    JenkinsNotificationDto notification = new JenkinsNotificationDto();
    JenkinsJobInfo jobInfo = new JenkinsJobInfo();

    JenkinsMessageVariable.JOB_NAME.setValue(notification, "myJob");
    JenkinsMessageVariable.BUILD_NUMBER.setValue(notification, "5");
    JenkinsMessageVariable.BUILD_PHASE.setValue(notification, "STARTED");
    JenkinsMessageVariable.BUILD_STATUS.setValue(notification, "SUCCESS");

    String urlJobPath = "/job/myJob/5";
    String jenkinsJobUrl = WireMockJenkins.HOST_URL + urlJobPath;
    JenkinsMessageVariable.BUILD_FULL_URL.setValue(notification, jenkinsJobUrl);

    JenkinsMessageVariable.JOB_USER.setValue(jobInfo, "user.name");
    JenkinsMessageVariable.JOB_PARAMETERS.setValue(jobInfo, "GIT_PROJECT=j2d-notification");
    JenkinsMessageVariable.JOB_PARAMETERS.setValue(jobInfo, "GIT_BRANCH=develop");

    String authHeaderAdminAdmin = "Basic YWRtaW46YWRtaW4=";
    ObjectMapper mapper = new ObjectMapper();
    String jobInfoJson = mapper.writeValueAsString(jobInfo);

    WireMockJenkins.getMock()
        .stubFor(get(urlMatching(urlJobPath + "/api/json\\?tree=.*"))
            .withHeader("Authorization", equalTo(authHeaderAdminAdmin))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jobInfoJson)
            ));

    MessageDto message = service.createMessage(notification);

    List<String> expectedLines = asList(
        "[TEST] Сборка myJob (#5): фаза - STARTED, статус - SUCCESS",
        "Ссылка на сборку: " + jenkinsJobUrl,
        "Кто запустил: user.name",
        "Параметры сборки: ",
        " - GIT_PROJECT: j2d-notification",
        " - GIT_BRANCH: develop");
    String expectedMessage = expectedLines.stream()
        .collect(Collectors.joining(System.lineSeparator()));
    assertThat(message.getContent(), is(expectedMessage));
    assertThat(message.getAvatarUrl(), is(DISCORD_AVATAR_URL));
    assertThat(message.getUsername(), is(DISCORD_USERNAME));
  }

  @Test
  @DisplayName("If error occurred - not ignored message returns with error in content")
  void createMessageWithError() throws JsonProcessingException {
    JenkinsNotificationDto notification = new JenkinsNotificationDto();
    JenkinsJobInfo jobInfo = new JenkinsJobInfo();

    JenkinsMessageVariable.JOB_NAME.setValue(notification, "myJob");
    JenkinsMessageVariable.BUILD_NUMBER.setValue(notification, "5");
    JenkinsMessageVariable.BUILD_PHASE.setValue(notification, "STARTED");
    JenkinsMessageVariable.BUILD_STATUS.setValue(notification, "SUCCESS");

    String urlJobPath = "/job/myJob/5";
    String jenkinsJobUrl = WireMockJenkins.HOST_URL + urlJobPath;
    JenkinsMessageVariable.BUILD_FULL_URL.setValue(notification, jenkinsJobUrl);

    JenkinsMessageVariable.JOB_USER.setValue(jobInfo, "user.name");
    JenkinsMessageVariable.JOB_PARAMETERS.setValue(jobInfo, "GIT_PROJECT=j2d-notification");
    JenkinsMessageVariable.JOB_PARAMETERS.setValue(jobInfo, "GIT_BRANCH=develop");

    String authHeaderAdminAdmin = "Basic YWRtaW46YWRtaW4=";
    ObjectMapper mapper = new ObjectMapper();
    String jobInfoJson = mapper.writeValueAsString(jobInfo);

    WireMockJenkins.getMock()
        .stubFor(get(urlMatching(urlJobPath + "/api/json\\?tree=.*"))
            .withHeader("Authorization", equalTo(authHeaderAdminAdmin))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jobInfoJson)
            ));

    String errorMessage = "can't read a file";
    doThrow(new RuntimeException(errorMessage))
        .when(messageTemplateReader).readTemplate();

    MessageDto message = service.createMessage(notification);

    List<String> expectedLines = asList(
        "[ERROR] Internal error occurred for a job 'myJob'",
        "java.lang.RuntimeException: can't read a file",
        "at com.github.shaart.jenkins2discord.notification");
    assertThat(message.isIgnored(), is(false));
    expectedLines.forEach(line -> assertThat(message.getContent(), containsString(line)));
    assertThat(message.getContent(), containsString(errorMessage));
    assertThat(message.getAvatarUrl(), is(DISCORD_AVATAR_URL));
    assertThat(message.getUsername(), is(DISCORD_USERNAME));
  }

  private void createMessageWithActiveFilters(List<String> jobParameters,
      List<JobFilter> jobFilters, Consumer<MessageDto> assertion) throws
      JsonProcessingException {
    JenkinsNotificationDto notification = new JenkinsNotificationDto();
    JenkinsJobInfo jobInfo = new JenkinsJobInfo();

    JenkinsMessageVariable.JOB_NAME.setValue(notification, A_JOB_NAME);
    JenkinsMessageVariable.BUILD_NUMBER.setValue(notification, "5");
    JenkinsMessageVariable.BUILD_PHASE.setValue(notification, "STARTED");
    JenkinsMessageVariable.BUILD_STATUS.setValue(notification, "SUCCESS");

    String urlJobPath = "/job/" + A_JOB_NAME + "/5";
    String jenkinsJobUrl = WireMockJenkins.HOST_URL + urlJobPath;
    JenkinsMessageVariable.BUILD_FULL_URL.setValue(notification, jenkinsJobUrl);

    JenkinsMessageVariable.JOB_USER.setValue(jobInfo, "user.name");
    jobParameters.forEach(parameter ->
        JenkinsMessageVariable.JOB_PARAMETERS.setValue(jobInfo, parameter));

    jenkinsProperties.setJobFilters(jobFilters);

    String authHeaderAdminAdmin = "Basic YWRtaW46YWRtaW4=";
    ObjectMapper mapper = new ObjectMapper();
    String jobInfoJson = mapper.writeValueAsString(jobInfo);

    WireMockJenkins.getMock()
        .stubFor(get(urlMatching(urlJobPath + "/api/json\\?tree=.*"))
            .withHeader("Authorization", equalTo(authHeaderAdminAdmin))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jobInfoJson)
            ));

    MessageDto message = service.createMessage(notification);

    assertThat("Message model should not be null in any case", message, not(nullValue()));
    assertion.accept(message);
  }
}