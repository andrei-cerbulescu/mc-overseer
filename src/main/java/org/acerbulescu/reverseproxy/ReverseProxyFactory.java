package org.acerbulescu.reverseproxy;

import com.google.inject.Inject;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;

public class ReverseProxyFactory {
  @Inject
  InstanceManager instanceManager;

  public ReverseProxy from(ServerInstance instance) {
    return ReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .serverInstance(instance)
        .build();
  }
}
