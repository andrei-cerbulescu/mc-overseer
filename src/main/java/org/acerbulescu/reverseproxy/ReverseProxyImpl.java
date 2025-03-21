package org.acerbulescu.reverseproxy;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;
import org.apache.logging.log4j.core.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

@Builder
@Log4j2
public class ReverseProxyImpl implements ReverseProxy {
  InstanceManager instanceManager;

  ServerInstance serverInstance;

  @Override
  public void start() {
    try {
      log.info("Creating reverse proxy for instance={} on port={}", serverInstance.getName(), serverInstance.getPublicPort());
      var serverSocket = new ServerSocket(serverInstance.getPublicPort());
      scheduleSuspend();
      while (true) {
        var clientSocket = serverSocket.accept();
        new Thread(() -> handleClient(clientSocket), "SERVER-" + serverInstance.getName() + "-PROXY").start();
      }
    } catch (IOException e) {
      log.error("Cannot create reverse proxy for instance={}", serverInstance.getName(), e);
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  private void handleClient(Socket clientSocket) {
    var clientUuid = UUID.randomUUID().toString();
    log.info("Client with uuid={} is connecting to instance={}", clientUuid, serverInstance.getName());
    if (serverInstance.getStatus(instanceManager.getTargetHost(serverInstance)).equals(ServerInstance.Status.SUSPENDED)) {
      instanceManager.resumeInstance(serverInstance);
    }

    var targetSocket = new Socket(instanceManager.getTargetHost(serverInstance), serverInstance.getPrivatePort());
    var clientInput = clientSocket.getInputStream();
    var clientOutput = clientSocket.getOutputStream();
    var targetInput = targetSocket.getInputStream();
    var targetOutput = targetSocket.getOutputStream();

    serverInstance.incrementConnectedPlayers();

    Thread forwardThread = new Thread(() -> forwardData(clientInput, targetOutput), "FORWARD-THREAD-" + serverInstance.getName() + "-" + UUID.randomUUID());
    Thread backwardThread = new Thread(() -> forwardData(targetInput, clientOutput), "BACKWARD-THREAD-" + serverInstance.getName() + "-" + UUID.randomUUID());

    forwardThread.start();
    backwardThread.start();

    forwardThread.join();

    clientSocket.close();
    targetSocket.close();

    serverInstance.decrementConnectedPlayers();
    log.info("Client with uuid={} disconnected from instance={}", clientUuid, serverInstance.getName());
    scheduleSuspend();
  }

  private void scheduleSuspend() {
    log.info("Scheduling instance={} for suspension", serverInstance.getName());
    try {
      Thread.sleep(10 * Constants.MILLIS_IN_SECONDS);
    } catch (InterruptedException e) {
      log.info("Could not await instance={} for suspension. Proceeding immediately", serverInstance.getName());
    }

    if (serverInstance.getConnectedPlayers().equals(0)) {
      instanceManager.suspendInstance(serverInstance);
    } else {
      log.info("Not suspending instance={} because it has active players", serverInstance.getName());
    }
  }

  private static void forwardData(InputStream input, OutputStream output) {
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
