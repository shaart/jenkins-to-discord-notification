package com.github.shaart.jenkins2discord.notification.exception;

public class StartupException extends RuntimeException {

  public StartupException(String errorMessage) {
    super(errorMessage);
  }
}
