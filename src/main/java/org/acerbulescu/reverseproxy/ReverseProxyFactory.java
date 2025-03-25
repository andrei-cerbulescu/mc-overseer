package org.acerbulescu.reverseproxy;

import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;
import org.springframework.stereotype.Component;

@Component
public class ReverseProxyFactory {

  public ReverseProxy from(InstanceManager instanceManager, ServerInstance instance) {
    return ReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .instanceName(instance.getName())
        .publicPort(instance.getPublicPort())
        .privatePort(instance.getPrivatePort())
        .build();
  }
}
