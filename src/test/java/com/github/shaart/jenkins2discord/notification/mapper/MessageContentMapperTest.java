package com.github.shaart.jenkins2discord.notification.mapper;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsBuildDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobActionInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobCauseInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsJobParameterInfo;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsVersionControlSystemDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

class MessageContentMapperTest {

  private static JenkinsJobInfo jobInfo;
  private static JenkinsNotificationDto notification;
  private static JenkinsVersionControlSystemDto versionControlSystem;
  private static JenkinsBuildDto jobBuild;

  private MessageContentMapper messageContentMapper;

  static Stream<Arguments> provideParseVariables() {
    return Stream.of(
        Arguments.of(1, "${JOB_NAME}",
            (Runnable) () -> notification.setJobName("testJob"),
            (Supplier<String>) () -> "testJob"),

        Arguments.of(2, "${BUILD_NUMBER}",
            (Runnable) () -> jobBuild.setNumber("159"),
            (Supplier<String>) () -> "159"),

        Arguments.of(3, "${BUILD_PHASE}",
            (Runnable) () -> jobBuild.setPhase("aPhase"),
            (Supplier<String>) () -> "aPhase"),

        Arguments.of(4, "${BUILD_STATUS}",
            (Runnable) () -> jobBuild.setStatus("aTestStatus"),
            (Supplier<String>) () -> "aTestStatus"),

        Arguments.of(5, "${BUILD_FULL_URL}",
            (Runnable) () -> jobBuild.setFullUrl("http://jenkins.com/myjob/5"),
            (Supplier<String>) () -> "http://jenkins.com/myjob/5"),

        Arguments.of(6, "${JOB_USER}",
            (Runnable) () -> jobInfo.getActions()
                .add(new JenkinsJobActionInfo("hudson.model.CauseAction",
                    null,
                    singletonList(new JenkinsJobCauseInfo("a.user.name")))),
            (Supplier<String>) () -> "a.user.name"),

        Arguments.of(7, "${JOB_PARAMETERS}",
            (Runnable) () -> jobInfo.getActions()
                .add(new JenkinsJobActionInfo("hudson.model.ParametersAction",
                    Arrays.asList(
                        new JenkinsJobParameterInfo("FIRST_PARAM", "a_value"),
                        new JenkinsJobParameterInfo("SECOND_PARAM", "second value")
                    ),
                    null
                )),
            (Supplier<String>) () -> System.lineSeparator()
                + " - FIRST_PARAM: a_value" + System.lineSeparator()
                + " - SECOND_PARAM: second value")
    );
  }

  @BeforeEach
  public void setUp() {
    Jenkins2DiscordProperties properties = Jenkins2DiscordProperties.empty();
    messageContentMapper = new MessageContentMapper(properties);

    jobInfo = new JenkinsJobInfo();
    jobInfo.setActions(new ArrayList<>());

    versionControlSystem = new JenkinsVersionControlSystemDto();

    jobBuild = new JenkinsBuildDto();
    jobBuild.setVersionControlSystem(versionControlSystem);

    notification = new JenkinsNotificationDto();
    notification.setJobBuild(jobBuild);
  }

  @ParameterizedTest
  @MethodSource("provideParseVariables")
  @DisplayName("Message content parses each message variable")
  void toMessageContentParsesVariables(int index, String paramTemplate, Runnable dataPreparer,
      Supplier<String> messageSupplier) {
    String templatePrefix = "[TEST] #" + index + " ";
    String template = templatePrefix + paramTemplate;

    dataPreparer.run();

    final String messageContent = messageContentMapper.toMessageContent(notification, jobInfo,
        template);

    assertThat(messageContent, Matchers.is(templatePrefix + messageSupplier.get()));
  }
}