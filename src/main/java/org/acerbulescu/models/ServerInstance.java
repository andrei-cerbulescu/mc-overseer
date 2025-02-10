package org.acerbulescu.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Status internalStatus = Status.UNHEALTHY;

  public enum Status {
    SUSPENDED("SUSPENDED"),
    HEALTHY("HEALTHY"),
    UNHEALTHY("UNHEALTHY");

    Status(final String text) {
    }
  }

  public void suspend() {
    internalStatus = Status.SUSPENDED;
  }

  public void resume() {
    internalStatus = Status.UNHEALTHY;
  }

  public Status getStatus(String host) {
    if (internalStatus.equals(Status.SUSPENDED)) {
      return Status.SUSPENDED;
    }

    if (isPortOpen(host, privatePort, 1000)) {
      internalStatus = Status.HEALTHY;
    } else {
      internalStatus = Status.UNHEALTHY;
    }

    return internalStatus;
  }

  public void incrementConnectedPlayers() {
    connectedPlayers++;
  }

  public void decrementConnectedPlayers() {
    connectedPlayers--;
  }

  private boolean isPortOpen(String host, int port, int timeout) {
    try (var socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

}
