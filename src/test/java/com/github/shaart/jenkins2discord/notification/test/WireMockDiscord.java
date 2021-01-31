package com.github.shaart.jenkins2discord.notification.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpStatus;

public class WireMockDiscord {

  public static final int DISCORD_MOCK_PORT = 8089;
  public static final String WEB_HOOK_URI = "/api/webhooks/"
      + "800303589935157777/"
      + "P2TQUq-LU28_qRzlMpF8KYbzCMw4cGXvIFxjslOrmUcikmmLTXn-WcuuWcuuWcuuWcuu";
  public static final String HOST_URL = "http://localhost" + ":" + DISCORD_MOCK_PORT;

  private static final WireMockServer discordMock = new WireMockServer(DISCORD_MOCK_PORT);

  public static void init() {
    String discordWebhookResponse = TestFileReader.getInstance()
        .readAsString("discordWebhookResponse.json");

    discordMock.stubFor(
        get(urlEqualTo(WEB_HOOK_URI))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(discordWebhookResponse)));

    discordMock.start();
  }

  public static void stop() {
    discordMock.stop();
  }

  public static void reset() {
    discordMock.resetRequests();
    discordMock.resetMappings();
    discordMock.resetScenarios();
    discordMock.resetToDefaultMappings();
  }
}
