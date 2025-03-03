package org.acerbulescu.reverseproxy;

import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;

public class ReverseProxyFactory {

  public ReverseProxy from(InstanceManager instanceManager, ServerInstance instance) {
    return ReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .serverInstance(instance)
        .build();
  }
}
