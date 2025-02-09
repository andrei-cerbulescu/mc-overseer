package org.acerbulescu.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ServerInstance {
  private String name;
  private Integer publicPort;
  private Integer privatePort;
  private Integer connectedPlayers = 0;
  private String path;
  private String startCommand;

  public enum HealthStatus {
    HEALTHY("HEALTHY"),
    UNHEALTHY("UNHEALTHY");

    HealthStatus(final String text) {
    }
  }

  public void incrementConnectedPlayers() {
    connectedPlayers++;
  }

  public void decrementConnectedPlayers() {
    connectedPlayers--;
  }
}
