package org.acerbulescu.reverseproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.acerbulescu.models.ServerInstance;

import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuperBuilder
public class TcpReverseProxyImpl extends ReverseProxy {
  ServerSocket serverSocket;

  private final AtomicInteger connectedPlayers = new AtomicInteger(0);

  @Override
  public void start() {
    try {
      log.info("Creating TCP proxy for instance={} on publicPort={} forwarding to privatePort={}", instanceName, publicPort, privatePort);
      serverSocket = new ServerSocket(publicPort);
      scheduleSuspend();
      while (!serverSocket.isClosed()) {
        var clientSocket = serverSocket.accept();
        new Thread(() -> handleClient(clientSocket), "SERVER-" + instanceName + "-PROXY").start();
      }
      log.info("Reverse proxy for instance={} on port={} has been closed", instanceName, publicPort);
    } catch (IOException e) {
      log.error("Cannot create reverse proxy for instance={}", instanceName, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
        log.info("Reverse proxy closed for instance={}", instanceName);
      }
    } catch (IOException e) {
      log.error("Error closing server socket for instance={}", instanceName, e);
    }
  }

  @Override
  public Status getStatus() {

    return connectedPlayers.get() == 0 ? Status.IDLE : Status.BUSY;
  }

  @SneakyThrows
  private void handleClient(Socket clientSocket) {
    var clientUuid = UUID.randomUUID().toString();
    log.info("Client with uuid={} is connecting to instance={}", clientUuid, instanceName);
    if (instanceManager.getInstanceStatus(instanceName).equals(ServerInstance.Status.SUSPENDED)) {
      instanceManager.resumeInstance(instanceName);
    }

    var targetSocket = new Socket(instanceManager.getTargetHost(instanceName), privatePort);
    var clientInput = clientSocket.getInputStream();
    var clientOutput = clientSocket.getOutputStream();
    var targetInput = targetSocket.getInputStream();
    var targetOutput = targetSocket.getOutputStream();

    connectedPlayers.incrementAndGet();

    Thread forwardThread = new Thread(() -> forwardData(clientInput, targetOutput), "FORWARD-THREAD-" + instanceName + "-" + UUID.randomUUID());
    Thread backwardThread = new Thread(() -> forwardData(targetInput, clientOutput), "BACKWARD-THREAD-" + instanceName + "-" + UUID.randomUUID());

    forwardThread.start();
    backwardThread.start();

    forwardThread.join();

    clientSocket.close();
    targetSocket.close();

    connectedPlayers.decrementAndGet();
    log.info("Client with uuid={} disconnected from instance={}", clientUuid, instanceName);
    scheduleSuspend();
  }

  private void forwardData(InputStream input, OutputStream output) {
    try {
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = input.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
        output.flush();
      }
    } catch (IOException ignored) {
    }
  }
}
