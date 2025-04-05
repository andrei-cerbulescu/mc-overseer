package org.acerbulescu.reverseproxy;

import lombok.AllArgsConstructor;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;
import org.springframework.stereotype.Component;

@Component
public class ReverseProxyFactory {

  @AllArgsConstructor
  public enum Protocol {
    TCP("TCP"), UDP("UDP");
    final String value;
  }

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
