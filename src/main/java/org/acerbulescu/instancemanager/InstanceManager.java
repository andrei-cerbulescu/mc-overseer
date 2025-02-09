package org.acerbulescu.instancemanager;

import org.acerbulescu.models.ServerInstance;

import java.util.List;

public interface InstanceManager {
  public ServerInstance getInstance(String name);

  public void shutdownAllInstances();

  public void startInstance(ServerInstance instanceName);

  public void suspendInstance(String instanceName);

  public void resumeInstance(String instanceName);

  public void stopInstance(String instanceName);

  public ServerInstance.HealthStatus getInstanceHealth(String instanceName);

  public String getTargetHost(String instanceName);

  public void startReverseProxy(ServerInstance instance);

  public void awaitHealthy(ServerInstance instance);
}
