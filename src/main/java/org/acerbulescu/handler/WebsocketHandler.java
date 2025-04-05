package org.acerbulescu.handler;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.docker.DockerClient;
import org.acerbulescu.instancemanager.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Log4j2
@Component
@AllArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {
  @Autowired
  private final DockerClient dockerClient;

  @Autowired
  InstanceManager instanceManager;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String instanceName = getInstanceName(session);

    log.info("Incrementing static connections for instance={}", instanceName);
    instanceManager.incrementStaticConnections(instanceName);
    dockerClient.attachToContainerIo(instanceName, session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String instanceName = getInstanceName(session);

    dockerClient.writeToContainerIo(instanceName, message.getPayload());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    String instanceName = getInstanceName(session);

    log.info("Decrementing static connections for instance={}", instanceName);
    instanceManager.decrementStaticConnections(instanceName);
  }

  private String getInstanceName(WebSocketSession session) {
    return (String) session.getAttributes().get("instanceName"); // depends on handshake
  }

}
