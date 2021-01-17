package com.github.shaart.jenkins2discord.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonResponseDto {

  private String message;

  public static CommonResponseDto createFail() {
    return CommonResponseDto.builder()
        .message("FAIL")
        .build();
  }

  public static CommonResponseDto createSuccess() {
    return CommonResponseDto.builder()
        .message("SUCCESS")
        .build();
  }
}
