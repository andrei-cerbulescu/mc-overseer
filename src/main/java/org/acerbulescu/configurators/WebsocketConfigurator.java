package org.acerbulescu.configurators;

import java.util.Map;

import org.acerbulescu.handler.WebsocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSocket
public class WebsocketConfigurator implements WebSocketConfigurer {
  private final WebsocketHandler handler;

  public WebsocketConfigurator(WebsocketHandler handler) {
    this.handler = handler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(handler, "/api/instance/{instanceName}/console")
        .addInterceptors(new HttpSessionHandshakeInterceptor() {
          @Override
          public boolean beforeHandshake(
              ServerHttpRequest request,
              ServerHttpResponse response,
              WebSocketHandler wsHandler,
              Map<String, Object> attributes) throws Exception {
            String instanceName = UriComponentsBuilder.fromPath(request.getURI().getPath())
                .build()
                .getPathSegments()
                .get(2);
            attributes.put("instanceName", instanceName);
            return true;
          }
        })
        .setAllowedOrigins("*");
  }
}
