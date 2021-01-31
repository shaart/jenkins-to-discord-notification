package com.github.shaart.jenkins2discord.notification.test;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockJenkins {

  public static final int JENKINS_MOCK_PORT = 8099;
  public static final String HOST_URL = "http://localhost" + ":" + JENKINS_MOCK_PORT;
  private static final WireMockServer jenkinsMock = new WireMockServer(JENKINS_MOCK_PORT);

  public static void init() {
    jenkinsMock.start();
  }

  public static void stop() {
    jenkinsMock.stop();
  }

  public static void reset() {
    jenkinsMock.resetRequests();
    jenkinsMock.resetMappings();
    jenkinsMock.resetScenarios();
    jenkinsMock.resetToDefaultMappings();
  }

  public static WireMockServer getMock() {
    return jenkinsMock;
  }
}
