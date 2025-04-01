package org.acerbulescu.models;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DockerInstance extends ServerInstance {
  private String id;

  public DockerInstance(ServerInstance s) {
    super(
        s.getName(),
        s.getPublicPort(),
        s.getPrivatePort(),
        s.getPath(),
        s.getStartCommand(),
        s.getStatus(),
        s.getHost(),
        s.getReverseProxies()
    );
  }
}
