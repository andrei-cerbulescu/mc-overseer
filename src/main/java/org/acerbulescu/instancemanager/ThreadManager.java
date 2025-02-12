package org.acerbulescu.instancemanager;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ThreadInstance;
import org.acerbulescu.processmanager.ProcessManager;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ThreadManager implements InstanceManager {

  @Inject
  ReverseProxyFactory reverseProxyFactory;

  @Inject
  ProcessManager processManager;

  List<ThreadInstance> instances = new ArrayList<>();

  @Override
  public ThreadInstance getInstance(String name) {
    return instances.stream()
        .filter(e -> e.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }

  @Override
  public void shutdownAllInstances() {
    instances.forEach(this::stopInstance);
  }

  @Override
  public void startInstance(ServerInstance instance) {
    var threadInstance = new ThreadInstance(instance);

    var thread = new Thread(() -> createInstanceThread(threadInstance), "SERVER-" + instance.getName());
    threadInstance.setThread(thread);

    instances.add(threadInstance);

    thread.start();
  }

  private void createInstanceThread(ThreadInstance instance) {
    var processBuilder = new ProcessBuilder(processManager.getShell());
    processBuilder.directory(new File(instance.getPath()));

    try {
      Process process = processBuilder.start();

      var writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      instance.setWriter(writer);
      instance.setReader(reader);
      instance.setErrorReader(errorReader);

      sendCommand(writer, instance.getStartCommand());

      var exitCode = process.waitFor();
      log.info("Instance " + instance.getName() + " exited with code: " + exitCode);
    } catch (Exception e) {
      log.error("Instance " + instance.getName() + " could not start due to error: ", e);
    }
  }

  @Override
  public void suspendInstance(ServerInstance instance) {
    if (!instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.SUSPENDED)) {
      var threadInstance = getInstance(instance.getName());
      processManager.suspendThread(threadInstance);
      threadInstance.suspend();
    }
  }

  @Override
  public void resumeInstance(ServerInstance instance) {
    var threadInstance = getInstance(instance.getName());

    if (instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.SUSPENDED)) {
      processManager.resumeThread(threadInstance);
      instance.resume();
    }
    try {
      while (!threadInstance.getStatus(getTargetHost(threadInstance)).equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      log.error("Could not await instance to be healthy: " + instance.getName(), e);
    }
  }

  @Override
  public void stopInstance(ServerInstance instance) {
    var threadInstance = getInstance(instance.getName());
    resumeInstance(instance);

    log.info("Stopping instance: " + instance.getName());

    try {
      sendCommand(threadInstance.getWriter(), "stop");
    } catch (Exception e) {
      log.error("Instance " + threadInstance.getName() + " cannot be stopped: ", e);
    }
  }

  @Override
  public String getTargetHost(ServerInstance instance) {
    return "127.0.0.1";
  }

  @Override
  public void startReverseProxy(ServerInstance instance) {
    new Thread(() -> reverseProxyFactory.from(instance).start(), "SERVER-" + instance.getName() + "-PROXY-MANAGER").start();
  }

  @Override
  public void awaitHealthy(ServerInstance instance) {
    log.info("Awaiting instance to be healthy: " + instance.getName());
    try {
      while (!instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Instance is healthy: " + instance.getName());
  }

  @Override
  public void scheduleSuspend(ServerInstance instance) {
    log.info("Scheduling instance for suspension: " + instance.getName());
    try {
      Thread.sleep(5 * 1000);
    } catch (InterruptedException e) {
      log.error("Could not sleep before suspending: ", e);
    }

    suspendInstance(instance);
  }

  private void sendCommand(BufferedWriter writer, String command) throws IOException {
    writer.write(command);
    writer.newLine();
    writer.flush();
  }
}
