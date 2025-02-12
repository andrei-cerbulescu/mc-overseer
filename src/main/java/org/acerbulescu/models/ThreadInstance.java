package org.acerbulescu.models;

import lombok.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThreadInstance extends ServerInstance {

  private Thread thread;

  private BufferedWriter writer;

  private BufferedReader reader;

  private BufferedReader errorReader;

  public ThreadInstance(ServerInstance s) {
    super(
        s.getName(),
        s.getPublicPort(),
        s.getPrivatePort(),
        s.getConnectedPlayers(),
        s.getPath(),
        s.getStartCommand(),
        Status.UNHEALTHY
    );
  }
}
