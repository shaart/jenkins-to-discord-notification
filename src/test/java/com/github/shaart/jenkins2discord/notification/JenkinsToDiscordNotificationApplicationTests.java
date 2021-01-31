package com.github.shaart.jenkins2discord.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.shaart.jenkins2discord.notification.auto.impl.WebHookValidator;
import com.github.shaart.jenkins2discord.notification.test.WireMockDiscord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class JenkinsToDiscordNotificationApplicationTests {

  /**
   * {@link WebHookValidator#validate()} creates a request on app context up. This is needed to
   * prevent context fail.
   */
  @BeforeAll
  public static void setUpDiscordWireMock() {
    WireMockDiscord.init();
  }

  @AfterAll
  public static void tearDownDiscordWireMock() {
    WireMockDiscord.stop();
    WireMockDiscord.reset();
  }

  @Test
  void contextLoads() {
    assertTrue(true);
  }

}
