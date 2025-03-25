package org.acerbulescu.models;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerInstanceConfigRepresentation {
  private String name;
  private Long publicPort;
  private Long privatePort;
  private String path;
  private String startCommand;
}
