package com.github.shaart.jenkins2discord.notification.dto.jenkins;

import lombok.Data;

@Data
public class JenkinsVersionControlSystemDto {

  private String url;
  private String branch;
  private String commit;
}
