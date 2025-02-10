package org.acerbulescu.instancemanager;

import org.acerbulescu.models.ServerInstance;

public interface InstanceManager {
  public ServerInstance getInstance(String name);

  public void shutdownAllInstances();

  public void startInstance(ServerInstance instance);

  public void suspendInstance(ServerInstance instance);

  public void resumeInstance(ServerInstance instance);

  public void stopInstance(ServerInstance instance);

  public String getTargetHost(ServerInstance instance);

  public void startReverseProxy(ServerInstance instance);

  public void awaitHealthy(ServerInstance instance);

  void scheduleSuspend(ServerInstance instance);
}
