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

  private static final String LONG_EXCEPTION_MESSAGE = ""
      + "java.lang.IllegalArgumentException: URI is not absolute"
      + "        at java.base/java.net.URL.fromURI(Unknown Source) ~[na:na]"
      + "        at java.base/java.net.URI.toURL(Unknown Source) ~[na:na]"
      + "        at org.springframework.http.client.SimpleClientHttpRequestFactory.createRequest(SimpleClientHttpRequestFactory.java:145) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.http.client.InterceptingClientHttpRequest$InterceptingRequestExecution.execute(InterceptingClientHttpRequest.java:98) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.boot.actuate.metrics.web.client.MetricsClientHttpRequestInterceptor.intercept(MetricsClientHttpRequestInterceptor.java:81) ~[spring-boot-actuator-2.4.2.jar!/:2.4.2]"
      + "        at org.springframework.http.client.InterceptingClientHttpRequest$InterceptingRequestExecution.execute(InterceptingClientHttpRequest.java:93) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.http.client.InterceptingClientHttpRequest.executeInternal(InterceptingClientHttpRequest.java:77) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.http.client.AbstractBufferingClientHttpRequest.executeInternal(AbstractBufferingClientHttpRequest.java:48) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.http.client.AbstractClientHttpRequest.execute(AbstractClientHttpRequest.java:66) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:775) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:710) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.client.RestTemplate.exchange(RestTemplate.java:601) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at com.github.shaart.jenkins2discord.notification.service.impl.DefaultNotificationToMessageService.retrieveJobParameters(DefaultNotificationToMessageService.java:109) ~[classes!/:na]"
      + "        at com.github.shaart.jenkins2discord.notification.service.impl.DefaultNotificationToMessageService.createMessage(DefaultNotificationToMessageService.java:57) ~[classes!/:na]"
      + "        at com.github.shaart.jenkins2discord.notification.service.impl.DefaultMessageService.sendMessage(DefaultMessageService.java:29) ~[classes!/:na]"
      + "        at com.github.shaart.jenkins2discord.notification.controller.MessageController.sendMessage(MessageController.java:21) ~[classes!/:na]"
      + "        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]"
      + "        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source) ~[na:na]"
      + "        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source) ~[na:na]"
      + "        at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]"
      + "        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:894) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1060) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:962) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at javax.servlet.http.HttpServlet.service(HttpServlet.java:652) ~[tomcat-embed-core-9.0.41.jar!/:4.0.FR]"
      + "        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883) ~[spring-webmvc-5.3.3.jar!/:5.3.3]"
      + "        at javax.servlet.http.HttpServlet.service(HttpServlet.java:733) ~[tomcat-embed-core-9.0.41.jar!/:4.0.FR]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:93) ~[spring-boot-actuator-2.4.2.jar!/:2.4.2]"
      + "        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.3.3.jar!/:5.3.3]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:888) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1597) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]"
      + "        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]"
      + "        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) ~[tomcat-embed-core-9.0.41.jar!/:9.0.41]"
      + "        at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]";

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