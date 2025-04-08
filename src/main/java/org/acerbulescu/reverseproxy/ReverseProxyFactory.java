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

  public ReverseProxy from(InstanceManager instanceManager, String instanceName, Integer publicPort,
      Integer privatePort, Protocol protocol) {
    if (protocol.equals(Protocol.TCP)) {
      return tcpFrom(instanceManager, instanceName, publicPort,
          privatePort, protocol);
    }

    return udpFrom(instanceManager, instanceName, publicPort, privatePort, protocol);
  }

  private ReverseProxy tcpFrom(InstanceManager instanceManager, String instanceName, Integer publicPort,
      Integer privatePort, Protocol protocol) {
    return TcpReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .instanceName(instanceName)
        .publicPort(publicPort)
        .privatePort(privatePort)
        .build();
  }

  private ReverseProxy udpFrom(InstanceManager instanceManager, String instanceName, Integer publicPort,
      Integer privatePort, Protocol protocol) {
    return UdpReverseProxyImpl.builder()
        .instanceManager(instanceManager)
        .instanceName(instanceName)
        .publicPort(publicPort)
        .privatePort(privatePort)
        .build();
  }
}
