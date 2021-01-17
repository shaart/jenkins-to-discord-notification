package com.github.shaart.jenkins2discord.notification.exception;

public class ValidationException extends RuntimeException {

  public ValidationException(String errorMessage) {
    super(errorMessage);
  }
}
