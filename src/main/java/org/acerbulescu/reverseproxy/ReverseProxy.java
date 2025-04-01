package org.acerbulescu.reverseproxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.acerbulescu.instancemanager.InstanceManager;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ReverseProxy {
  InstanceManager instanceManager;

  String instanceName;
  Integer publicPort;
  Integer privatePort;

  public abstract void start();

  public abstract void stop();

  public abstract Status getStatus();

  public enum Status {
    BUSY, IDLE,
  }
}
