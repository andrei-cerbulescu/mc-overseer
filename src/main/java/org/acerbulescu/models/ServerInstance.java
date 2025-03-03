package org.acerbulescu.models;

import lombok.*;
import me.dilley.MineStat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

  @Getter(AccessLevel.PROTECTED)
  @Setter(AccessLevel.NONE)
  private final AtomicBoolean isSuspended = new AtomicBoolean(false);
  @Getter(AccessLevel.PROTECTED)
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
    isSuspended.set(true);
  }

  public void resume() {
    isSuspended.set(false);
  }

  public Status getStatus(String host) {
    if (isSuspended.get()) {
      return Status.SUSPENDED;
    }

    if (new MineStat(host, privatePort, 1).isServerUp()) {
      return Status.HEALTHY;
    } else {
      return Status.UNHEALTHY;
    }
  }

  public void incrementConnectedPlayers() {
    connectedPlayers.incrementAndGet();
  }

  public void decrementConnectedPlayers() {
    connectedPlayers.decrementAndGet();
  }

  public Integer getConnectedPlayers() {
    return connectedPlayers.get();
  }

}
