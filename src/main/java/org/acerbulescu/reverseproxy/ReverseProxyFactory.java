package org.acerbulescu.reverseproxy;

import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;
import org.springframework.stereotype.Component;

@Component
public class ReverseProxyFactory {

  public enum Protocol {TCP, UDP}

  public ReverseProxy from(InstanceManager instanceManager, ServerInstance instance, Protocol protocol) {
    if (protocol.equals(Protocol.TCP)) {
      return tcpFrom(instance, instanceManager);
    }

    return udpFrom(instance, instanceManager);
  }

  private ReverseProxy tcpFrom(ServerInstance instance, InstanceManager instanceManager) {
    return TcpReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .instanceName(instance.getName())
        .publicPort(instance.getPublicPort())
        .privatePort(instance.getPrivatePort())
        .build();
  }

  private ReverseProxy udpFrom(ServerInstance instance, InstanceManager instanceManager) {
    return UdpReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .instanceName(instance.getName())
        .publicPort(instance.getPublicPort())
        .privatePort(instance.getPrivatePort())
        .build();
  }
}
