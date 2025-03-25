package org.acerbulescu.models;

import org.acerbulescu.models.ServerInstance.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerInstanceRepresentation {
  private String name;
  private Integer publicPort;
  private Integer privatePort;
  private String path;
  private String startCommand;
  private Status status;
  private Integer connectedPlayers;
}
