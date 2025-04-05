package org.acerbulescu.instancemanager;

import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;
import org.acerbulescu.models.ThreadInstance;
import org.acerbulescu.processmanager.ProcessManager;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Deprecated
public class ThreadManager {
  ReverseProxyFactory reverseProxyFactory;

  ProcessManager processManager;

  List<ThreadInstance> instances = new ArrayList<>();


  public ThreadInstance getInstance(String name) {
    return instances.stream()
        .filter(e -> e.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }


  public List<ServerInstance> getInstances() {
    return List.of();
  }


  public void shutdownAllInstances() {
    instances.forEach(this::stopInstance);
  }


  public void startInstance(ServerInstance instance) {
    var threadInstance = new ThreadInstance(instance);

    var thread = new Thread(() -> createInstanceThread(threadInstance), "SERVER-" + instance.getName());
    threadInstance.setThread(thread);

    instances.add(threadInstance);

    thread.start();

    awaitHealthy(instance);
    startReverseProxy(instance);
  }

  private void createInstanceThread(ThreadInstance instance) {
    var processBuilder = new ProcessBuilder(instance.getStartCommand().split(" "));
    processBuilder.directory(new File(instance.getPath()));

    try {
      Process process = processBuilder.start();

      var writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      instance.setWriter(writer);
      instance.setReader(reader);
      instance.setErrorReader(errorReader);

      var exitCode = process.waitFor();
      log.info("Instance={} exited with code={}", instance.getName(), exitCode);
    } catch (Exception e) {
      log.error("Instance={} could not start due to error: ", instance.getName(), e);
    }
  }


  public void suspendInstance(ServerInstance instance) {
    if (!instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      var threadInstance = getInstance(instance.getName());
      processManager.suspendThread(threadInstance);
      threadInstance.suspend();
    }
  }


  public void resumeInstance(ServerInstance instance) {
    var threadInstance = getInstance(instance.getName());

    if (instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      processManager.resumeThread(threadInstance);
      instance.resume();
    }
    try {
      while (!threadInstance.getStatus().equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      log.error("Could not await instance to be healthy={}", instance.getName(), e);
    }
  }


  public void stopInstance(ServerInstance instance) {
    var threadInstance = getInstance(instance.getName());
    resumeInstance(instance);

    log.info("Stopping instance: " + instance.getName());

    try {
      sendCommand(threadInstance.getWriter(), "stop");
    } catch (Exception e) {
      log.error("Instance={} cannot be stopped", threadInstance.getName(), e);
    }
  }


  public String getTargetHost(ServerInstance instance) {
    return "127.0.0.1";
  }


  public String getTargetHost(ServerInstanceConfigRepresentation instance) {
    return "127.0.0.1";
  }

  private void startReverseProxy(ServerInstance instance) {
    // new Thread(() -> reverseProxyFactory.from(this, instance).start(), "SERVER-" + instance.getName() + "-PROXY-MANAGER").start();
  }

  private void awaitHealthy(ServerInstance instance) {
    log.info("Awaiting instance={} to be healthy", instance.getName());
    try {
      while (!instance.getStatus().equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Instance={} is healthy", instance.getName());
  }

  private void sendCommand(BufferedWriter writer, String command) throws IOException {
    writer.write(command);
    writer.newLine();
    writer.flush();
  }
}
