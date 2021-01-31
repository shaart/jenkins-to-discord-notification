package com.github.shaart.jenkins2discord.notification.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Jenkins;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.MessageInfo;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.User;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Webhook;
import com.github.shaart.jenkins2discord.notification.reader.MessageTemplateReader;
import com.github.shaart.jenkins2discord.notification.test.WireMockDiscord;
import com.github.shaart.jenkins2discord.notification.test.WireMockJenkins;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class DefaultNotificationToMessageServiceTest {

  public static final String DISCORD_AVATAR_URL = "https://image.flaticon.com/icons/png/512/147/147144.png";
  public static final String DISCORD_USERNAME = "common.developer";
  private Jenkins2DiscordProperties properties;
  private RestTemplate restTemplate;
  private MessageContentMapper messageContentMapper;
  private MessageTemplateReader messageTemplateReader;

  private DefaultNotificationToMessageService service;

  @BeforeEach
  public void setUp() {
    properties = Mockito.spy(new Jenkins2DiscordProperties());
    restTemplate = Mockito.spy(new RestTemplate());
    messageContentMapper = Mockito.spy(new MessageContentMapper());
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

    Jenkins jenkinsProperties = new Jenkins();
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

    List<String> expectedLines = Arrays.asList(
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
}