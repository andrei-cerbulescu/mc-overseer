package org.acerbulescu.instancemanager;

import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;

import java.util.List;

public interface InstanceManager {
  ServerInstance getInstance(String name);

  List<ServerInstance> getInstances();

  void shutdownAllInstances();

  void startInstance(ServerInstance instance);

  void suspendInstance(String instanceName);

  void resumeInstance(String instanceName);

  void stopInstance(String instanceName);

  void incrementConnectedPlayers(String instanceName);

  void decrementConnectedPlayers(String instanceName);

  Integer getConnectedPlayers(String instanceName);

  ServerInstance.Status getInstanceStatus(String instanceName);

  String getTargetHost(String instanceName);

  String getTargetHost(ServerInstanceConfigRepresentation instance);
}
