package com.github.shaart.jenkins2discord.notification.handler.resttemplate;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class NoExceptionErrorHandler extends DefaultResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) {
    return false;
  }
}
