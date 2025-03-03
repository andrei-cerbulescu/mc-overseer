package org.acerbulescu.instancemanager;

import org.acerbulescu.models.ServerInstance;

public interface InstanceManager {
  ServerInstance getInstance(String name);

  void shutdownAllInstances();

  void startInstance(ServerInstance instance);

  void suspendInstance(ServerInstance instance);

  void resumeInstance(ServerInstance instance);

  void stopInstance(ServerInstance instance);

  String getTargetHost(ServerInstance instance);
}
