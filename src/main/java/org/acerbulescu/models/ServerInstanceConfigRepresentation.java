package org.acerbulescu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerInstanceConfigRepresentation {
  private String name;
  private Long publicPort;
  private Long privatePort;
  private String path;
  private String startCommand;

  @JsonSetter(nulls = Nulls.AS_EMPTY)
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
