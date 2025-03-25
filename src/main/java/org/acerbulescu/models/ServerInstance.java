package org.acerbulescu.models;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.dilley.MineStat;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerInstance {
  private String name;
  private Integer publicPort;
  private Integer privatePort;
  private String path;
  private String startCommand;
  private Status status;
  private String host;

  @Setter(AccessLevel.NONE)
  private final AtomicInteger connectedPlayers = new AtomicInteger(0);

  public enum Status {
    SUSPENDED("SUSPENDED"),
    HEALTHY("HEALTHY"),
    UNHEALTHY("UNHEALTHY");

    Status(final String text) {
    }
  }

  public void suspend() {
    status = Status.SUSPENDED;
  }

  public void resume() {
    status = new MineStat(host, privatePort, 1).isServerUp() ? Status.HEALTHY : Status.UNHEALTHY;
  }

  public Status getStatus() {
    if (status == null) {
      status = Status.UNHEALTHY;
    }
    
    if (status.equals(Status.SUSPENDED)) {
      return Status.SUSPENDED;
    }

    return new MineStat(host, privatePort, 1).isServerUp() ? Status.HEALTHY : Status.UNHEALTHY;
  }

  public void incrementConnectedPlayers() {
    connectedPlayers.incrementAndGet();
  }

  public void decrementConnectedPlayers() {
    connectedPlayers.decrementAndGet();
  }

  public int getConnectedPlayers() {
    return connectedPlayers.get();
  }

}
