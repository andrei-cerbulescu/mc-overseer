package org.acerbulescu.instancemanager;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ThreadInstance;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

import javax.annotation.PreDestroy;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ThreadManager implements InstanceManager {

  @Inject
  ReverseProxyFactory reverseProxyFactory;

  List<ThreadInstance> instances = new ArrayList<>();

  private ThreadInstance getThreadInstance(String name) {
    return instances.stream()
        .filter(e -> e.getInstance().getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }

  @Override
  public ServerInstance getInstance(String name) {
    return getThreadInstance(name)
        .getInstance();
  }

  @Override
  public void shutdownAllInstances() {
    instances.forEach(e -> {
      try {
        log.info("Sending command for shutdown for instance: " + e.getInstance().getName());
        sendCommand(e.getWriter(), "stop");
      } catch (IOException ex) {
        log.error("Instance " + e.getInstance().getName() + " could not be shutdown: ", e);
      }
    });
  }

  @Override
  public void startInstance(ServerInstance instance) {
    var threadInstance = new ThreadInstance();
    threadInstance.setInstance(instance);

    var thread = new Thread(() -> {
      createInstanceThread(threadInstance);
    }, "SERVER-" + instance.getName());
    threadInstance.setThread(thread);

    instances.add(threadInstance);

    thread.start();
  }

  private void createInstanceThread(ThreadInstance instance) {
    var processBuilder = new ProcessBuilder(getShell());
    processBuilder.directory(new File(instance.getInstance().getPath()));

    try {
      Process process = processBuilder.start();

      var writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      instance.setWriter(writer);
      instance.setReader(reader);
      instance.setErrorReader(errorReader);

      sendCommand(writer, instance.getInstance().getStartCommand());

      var exitCode = process.waitFor();
      log.info("Instance " + instance.getInstance().getName() + " exited with code: " + exitCode);
    } catch (Exception e) {
      log.error("Instance " + instance.getInstance().getName() + " could not start due to error: ", e);
    }
  }

  @Override
  public void suspendInstance(String instanceName) {
    var threadInstance = getThreadInstance(instanceName);

    //noinspection deprecation
    threadInstance.getThread().suspend();
  }

  @Override
  public void resumeInstance(String instanceName) {
    var threadInstance = getThreadInstance(instanceName);

    //noinspection deprecation
    threadInstance.getThread().resume();
  }

  @Override
  public void stopInstance(String instanceName) {
    var threadInstance = getThreadInstance(instanceName);
    resumeInstance(instanceName);

    try {
      sendCommand(threadInstance.getWriter(), "stop");
    } catch (Exception e) {
      log.error("Instance " + threadInstance.getInstance().getName() + " cannot be stopped: ", e);
    }
  }

  @Override
  public ServerInstance.HealthStatus getInstanceHealth(String instanceName) {
    var instance = getThreadInstance(instanceName).getInstance();
    try {
      var address = InetAddress.getByName("127.0.0.1:" + instance.getPrivatePort().toString());
      if (address.isReachable(2000)) {
        return ServerInstance.HealthStatus.HEALTHY;
      }

      return ServerInstance.HealthStatus.UNHEALTHY;
    } catch (Exception e) {
      return ServerInstance.HealthStatus.UNHEALTHY;
    }
  }

  @Override
  public String getTargetHost(String instanceName) {
    return "127.0.0.1";
  }

  @Override
  public void startReverseProxy(ServerInstance instance) {
    new Thread(() -> {
      reverseProxyFactory.from(instance).start();
    }, "SERVER-" + instance.getName() + "-PROXY-MANAGER").start();
  }

  @Override
  public void awaitHealthy(ServerInstance instance) {
    log.info("Awaiting instance to be healthy: " + instance.getName());
    try {
      while (!isPortOpen(getTargetHost(instance.getName()), instance.getPrivatePort(), 10 * 60 * 1000)) {
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Instance is healthy: " + instance.getName());
  }

  public static boolean isPortOpen(String host, int port, int timeout) {
    try (var socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true; // Port is open
    } catch (IOException e) {
      return false; // Port is closed or unreachable
    }
  }

  private static String getShell() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("win") ? "cmd.exe" : "/bin/sh";
  }

  private void sendCommand(BufferedWriter writer, String command) throws IOException {
    writer.write(command);
    writer.newLine();
    writer.flush();
  }
}
