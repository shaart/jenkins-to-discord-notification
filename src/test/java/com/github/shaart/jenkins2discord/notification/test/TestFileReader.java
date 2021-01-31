package com.github.shaart.jenkins2discord.notification.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class TestFileReader {

  private static TestFileReader INSTANCE = new TestFileReader();

  public static TestFileReader getInstance() {
    return INSTANCE;
  }

  public String readAsString(String relativeFilePath) {
    Path testFilePath = Paths.get("src",
        "test",
        "resources",
        relativeFilePath);

    try {
      return Files.readAllLines(testFilePath)
          .stream()
          .collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new IllegalArgumentException("Can't read file '" + testFilePath + "'", e);
    }
  }
}
