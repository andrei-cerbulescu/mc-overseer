package org.acerbulescu.reverseproxy;

import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.ExecutorService;

@Log4j2
@SuperBuilder
public class UdpReverseProxyImpl extends ReverseProxy {
  private static final Long NO_REQUEST_TIMEOUT = 10L;

  private final ExecutorService executor;
  private DatagramSocket socket;
  private boolean running;
  private Instant lastRequest;

  @Override
  public void start() {
    running = true;
    executor.submit(this::runProxy);
    log.info("Creating UDP proxy for instance={} on publicPort={} forwarding to privatePort={}", instanceName, publicPort, privatePort);
  }

  private void runProxy() {
    try {
      var backendAddress = InetAddress.getByName(instanceManager.getTargetHost(instanceName));
      socket = new DatagramSocket(publicPort);
      byte[] buffer = new byte[4096];

      while (running) {
        var requestPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(requestPacket);

        var forwardPacket = new DatagramPacket(
            requestPacket.getData(),
            requestPacket.getLength(),
            backendAddress,
            privatePort
        );
        socket.send(forwardPacket);

        var responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);

        var clientPacket = new DatagramPacket(
            responsePacket.getData(),
            responsePacket.getLength(),
            requestPacket.getAddress(),
            requestPacket.getPort()
        );
        socket.send(clientPacket);
        lastRequest = Instant.now();
      }
    } catch (IOException e) {
      log.error("UDP proxy for instance={} ran into an error: ", instanceName, e);
      throw new RuntimeException(e);
    } finally {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    }
  }

  @Override
  public void stop() {
    running = false;
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
    executor.shutdown();
    log.info("UDP proxy for instance={} stopped", instanceName);
  }

  @Override
  public Status getStatus() {
    var now = Instant.now();
    if (lastRequest.plusSeconds(NO_REQUEST_TIMEOUT).isBefore(now)) {
      return Status.IDLE;
    }

    return Status.BUSY;
  }
}
