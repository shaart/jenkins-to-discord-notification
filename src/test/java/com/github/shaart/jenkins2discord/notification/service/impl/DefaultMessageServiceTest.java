package com.github.shaart.jenkins2discord.notification.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static wiremock.com.google.common.base.Strings.repeat;

import com.github.shaart.jenkins2discord.notification.dto.CommonResponseDto;
import com.github.shaart.jenkins2discord.notification.dto.discord.MessageDto;
import com.github.shaart.jenkins2discord.notification.dto.jenkins.JenkinsNotificationDto;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Discord;
import com.github.shaart.jenkins2discord.notification.properties.Jenkins2DiscordProperties.Webhook;
import com.github.shaart.jenkins2discord.notification.service.NotificationToMessageService;
import com.github.shaart.jenkins2discord.notification.strings.StringAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class DefaultMessageServiceTest {

  public static final String WEBHOOK_URL = "http://sample.url/abc";
  public static final JenkinsNotificationDto NOTIFICATION = JenkinsNotificationDto.builder()
      .build();

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private Jenkins2DiscordProperties properties;

  @Mock
  private NotificationToMessageService notificationToMessageService;

  @Spy
  private StringAnalyzer stringAnalyzer;

  @InjectMocks
  private DefaultMessageService messageService;

  @BeforeEach
  public void setUp() {
    Webhook webhookSettings = new Webhook();
    webhookSettings.setUrl(WEBHOOK_URL);

    Discord discordSettings = new Discord();
    discordSettings.setWebhook(webhookSettings);

    when(properties.getDiscord())
        .thenReturn(discordSettings);

    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(ResponseEntity.ok("{\"status\":\"OK\"}"));
  }

  @Test
  @DisplayName("Common short message sends successfully without splitting")
  void sendMessageShortMessage() {
    String initialContent = "SAMPLE CONTENT";

    MessageDto sampleContent = MessageDto.builder()
        .isIgnored(false)
        .content(initialContent)
        .avatarUrl(null)
        .username(null)
        .build();
    makeReturnMessage(sampleContent);

    CommonResponseDto commonResponseDto = messageService.sendMessage(NOTIFICATION);

    assertThat(commonResponseDto.getMessage(), equalTo("SUCCESS"));

    ArgumentMatcher<MessageDto> hasSameMessage = messageWithContent(initialContent);

    verify(restTemplate, times(1))
        .postForEntity(eq(WEBHOOK_URL), argThat(hasSameMessage), eq(String.class));
  }

  @Test
  @DisplayName("Long message 2002 symbols sends with splitting to 2000 and 2 symbols messages")
  void sendMessageLongSimpleMessage2002() {
    String firstPart = repeat("1", 2000);
    String secondPart = repeat("2", 2);
    String initialContent = firstPart + secondPart;

    MessageDto sampleContent = MessageDto.builder()
        .isIgnored(false)
        .content(initialContent)
        .avatarUrl(null)
        .username(null)
        .build();
    makeReturnMessage(sampleContent);

    CommonResponseDto commonResponseDto = messageService.sendMessage(NOTIFICATION);

    assertThat(commonResponseDto.getMessage(), equalTo("SUCCESS"));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(firstPart)),
            eq(String.class));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(secondPart)),
            eq(String.class));
  }

  @Test
  @DisplayName("Long message 4040 symbols sends with splitting to 2000, 2000 and 40 symbols "
      + "messages")
  void sendMessageLongSimpleMessage4040() {
    String firstPart = repeat("1", 2000);
    String secondPart = repeat("2", 2000);
    String thirdPart = repeat("3", 40);
    String initialContent = firstPart + secondPart + thirdPart;

    MessageDto sampleContent = MessageDto.builder()
        .isIgnored(false)
        .content(initialContent)
        .avatarUrl(null)
        .username(null)
        .build();
    makeReturnMessage(sampleContent);

    CommonResponseDto commonResponseDto = messageService.sendMessage(NOTIFICATION);

    assertThat(commonResponseDto.getMessage(), equalTo("SUCCESS"));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(firstPart)),
            eq(String.class));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(secondPart)),
            eq(String.class));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(thirdPart)),
            eq(String.class));
  }

  @Test
  @DisplayName("Long message 2030 symbols with preformatting at 1970-2010 sends with splitting "
      + "to 1970 and 40 symbols messages")
  void sendMessageLongMessage2030Preformatted() {
    String firstPart = repeat("1", 1970);
    String preformatting = StringAnalyzer.DISCORD_PREFORMATTED_TEXT;
    int secondPartLength = 2010 - 1970;
    int preformattedContentLength = secondPartLength - (preformatting.length() * 2);
    String secondPart = preformatting + repeat("2", preformattedContentLength) + preformatting;
    String initialContent = firstPart + secondPart;

    MessageDto sampleContent = MessageDto.builder()
        .isIgnored(false)
        .content(initialContent)
        .avatarUrl(null)
        .username(null)
        .build();
    makeReturnMessage(sampleContent);

    CommonResponseDto commonResponseDto = messageService.sendMessage(NOTIFICATION);

    assertThat(commonResponseDto.getMessage(), equalTo("SUCCESS"));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(firstPart)),
            eq(String.class));

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(secondPart)),
            eq(String.class));
  }

  @Test
  @DisplayName("Long message 4030 symbols with preformatting at 1970-4015 (more than 1 message) "
      + "sends with splitting to 1970, 2000 with preformatting (6 symb total) and 66 symbols "
      + "messages (15 from third part, 6 preformatting, 45 from second part")
  // (1) 1970: 111..111
  // (2) 2000: ```222..222``` ("222" at the end replaced with "```")
  // +3 ("222") +3 ("```" at the start) = +6
  // (3) 4030-1970-2000=60 +6 (from (2)) = 66 of "```" (3), "22..22" (45), "```" (3), "33..33" (15 total)
  void sendMessageLongMessage4030LongPreformatted() {
    String firstPart = repeat("1", 1970);
    String preformatting = StringAnalyzer.DISCORD_PREFORMATTED_TEXT;
    int secondPartLength = 4015 - 1970;
    int preformattedContentLength = secondPartLength - (preformatting.length() * 2);
    String secondPart = preformatting + repeat("2", preformattedContentLength) + preformatting;
    String thirdPart = repeat("3", 4030 - 4015);
    String initialContent = firstPart + secondPart + thirdPart;

    MessageDto sampleContent = MessageDto.builder()
        .isIgnored(false)
        .content(initialContent)
        .avatarUrl(null)
        .username(null)
        .build();
    makeReturnMessage(sampleContent);

    CommonResponseDto commonResponseDto = messageService.sendMessage(NOTIFICATION);

    assertThat(commonResponseDto.getMessage(), equalTo("SUCCESS"));

    String expectedFirstPart = repeat("1", 1970);

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(expectedFirstPart)),
            eq(String.class));

    int expectedSecondPartTextLength =
        DefaultMessageService.DISCORD_MAX_MESSAGE_LENGTH - (preformatting.length() * 2);
    String expectedSecondPart =
        preformatting + repeat("2", expectedSecondPartTextLength) + preformatting;

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(expectedSecondPart)),
            eq(String.class));

    String expectedThirdPart = StringAnalyzer.DISCORD_PREFORMATTED_TEXT
        + repeat("2", 45)
        + StringAnalyzer.DISCORD_PREFORMATTED_TEXT
        + repeat("3", 15);

    verify(restTemplate, times(1))
        .postForEntity(
            eq(WEBHOOK_URL),
            argThat(messageWithContent(expectedThirdPart)),
            eq(String.class));
  }

  private ArgumentMatcher<MessageDto> messageWithContent(String initialContent) {
    return argument ->
        initialContent.equals(argument.getContent());
  }

  private void makeReturnMessage(MessageDto sampleContent) {
    when(notificationToMessageService.createMessage(NOTIFICATION))
        .thenReturn(sampleContent);
  }
}