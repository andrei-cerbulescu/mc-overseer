package org.acerbulescu.instancemanager;

import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;

import java.util.List;

public interface InstanceManager {
  ServerInstance getInstance(String name);

  List<ServerInstance> getInstances();

  List<String> getInstancesNames();

  void shutdownAllInstances();

  void startInstance(ServerInstance instance);

  void startExistingInstance(String instanceName);

  void restartInstanceIfCrashed(String instanceName);

  void resumeInstance(String instanceName);

  void stopInstance(String instanceName);

  void attemptSuspend(String instanceName);

  ServerInstance.Status getInstanceStatus(String instanceName);

  String getTargetHost(String instanceName);

  String getTargetHost(ServerInstanceConfigRepresentation instance);

  void incrementStaticConnections(String instanceName);

  void decrementStaticConnections(String instanceName);
}
