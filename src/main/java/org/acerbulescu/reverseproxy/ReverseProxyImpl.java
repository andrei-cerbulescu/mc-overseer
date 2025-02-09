package org.acerbulescu.reverseproxy;

import com.google.inject.Inject;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;

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
      log.info("Creating reverse proxy for instance: " + serverInstance.getName());
      var serverSocket = new ServerSocket(serverInstance.getPublicPort());
      while (true) {
        var clientSocket = serverSocket.accept();
        new Thread(() -> handleClient(clientSocket), "SERVER-" + serverInstance.getName() + "-PROXY").start();
      }
    } catch (IOException e) {
      log.error("Cannot create reverse proxy for instance " + serverInstance.getName(), e);
    }
  }

  @SneakyThrows
  private void handleClient(Socket clientSocket) {
    var targetSocket = new Socket(instanceManager.getTargetHost(serverInstance.getName()), serverInstance.getPrivatePort());
    var clientInput = clientSocket.getInputStream();
    var clientOutput = clientSocket.getOutputStream();
    var targetInput = targetSocket.getInputStream();
    var targetOutput = targetSocket.getOutputStream();

    serverInstance.incrementConnectedPlayers();

    Thread forwardThread = new Thread(() -> forwardData(clientInput, targetOutput), "FORWARD-THREAD-" + serverInstance.getName() + "-" + UUID.randomUUID().toString());
    Thread backwardThread = new Thread(() -> forwardData(targetInput, clientOutput), "BACKWARD-THREAD-" + serverInstance.getName() + "-" + UUID.randomUUID().toString());

    forwardThread.start();
    backwardThread.start();

    forwardThread.join();
    backwardThread.join();

    serverInstance.decrementConnectedPlayers();
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
