package org.acerbulescu.models;

import lombok.*;
import me.dilley.MineStat;
import org.acerbulescu.reverseproxy.ReverseProxy;

import java.util.List;

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

  private List<ReverseProxy> reverseProxies;

  public enum Status {
    SUSPENDED("SUSPENDED"),
    HEALTHY("HEALTHY"),
    UNHEALTHY("UNHEALTHY"),
    SHUTDOWN("SHUTDOWN");

    Status(final String text) {
    }
  }

  public void stop() {
    status = Status.SHUTDOWN;
  }

  public void start() {
    status = Status.UNHEALTHY;
  }

  public void suspend() {
    if (status.equals(Status.SHUTDOWN)) {
      return;
    }

    status = Status.SUSPENDED;
  }

  public void resume() {
    if (status.equals(Status.SHUTDOWN)) {
      return;
    }

    status = new MineStat(host, privatePort, 1).isServerUp() ? Status.HEALTHY : Status.UNHEALTHY;
  }

  public Status getStatus() {
    if (status == null) {
      status = Status.UNHEALTHY;
    }

    if (status.equals(Status.SUSPENDED)) {
      return Status.SUSPENDED;
    }

    if (status.equals(Status.SHUTDOWN)) {
      return Status.SHUTDOWN;
    }

    return new MineStat(host, privatePort, 1).isServerUp() ? Status.HEALTHY : Status.UNHEALTHY;
  }

}
