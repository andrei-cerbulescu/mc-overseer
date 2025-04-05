package org.acerbulescu.models;

import lombok.*;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerInstanceConfigRepresentation {
  private String name;
  private Long publicPort;
  private Long privatePort;
  private String path;
  private String startCommand;
  private List<Ports> ports;

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Ports {
    private ReverseProxyFactory.Protocol protocol;
    private Integer publicPort;
    private Integer privatePort;
  }
}
