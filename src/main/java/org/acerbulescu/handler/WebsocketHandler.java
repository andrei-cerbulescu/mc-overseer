package org.acerbulescu.handler;

import lombok.AllArgsConstructor;
import org.acerbulescu.docker.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@AllArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {
  @Autowired
  private final DockerClient dockerClient;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String instanceName = getInstanceName(session);

    dockerClient.attachToContainerIo(instanceName, session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String instanceName = getInstanceName(session);

    dockerClient.writeToContainerIo(instanceName, message.getPayload());
  }

  private String getInstanceName(WebSocketSession session) {
    return (String) session.getAttributes().get("instanceName"); // depends on handshake
  }

}
