package org.acerbulescu.instancemanager;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.docker.DockerClient;
import org.acerbulescu.models.DockerInstance;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;
import org.apache.logging.log4j.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DockerInstanceManger implements InstanceManager {
  @Inject
  ReverseProxyFactory reverseProxyFactory;

  @Inject
  DockerClient dockerClient;

  List<DockerInstance> instances = new ArrayList<>();

  @Override
  public DockerInstance getInstance(String name) {
    return instances.stream()
        .filter(e -> e.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }

  @Override
  public void shutdownAllInstances() {
    log.info("Shutting down all instances");
    instances.forEach(this::stopInstance);
  }

  @Override
  @SneakyThrows
  public void startInstance(ServerInstance instance) {
    var containerInstance = createContainer(instance);

    instances.add(containerInstance);

    awaitHealthy(instance);

    startReverseProxy(instance);
  }

  DockerInstance createContainer(ServerInstance instance) {
    var container = dockerClient.createServerContainer(instance);

    var containerInstance = new DockerInstance(instance);

    containerInstance.setId(container.getId());
    dockerClient.startContainer(containerInstance);

    return containerInstance;
  }

  @Override
  public void suspendInstance(ServerInstance instance) {
    if (instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.SUSPENDED)) {
      log.info("Not suspending instance={} because it is already suspended", instance.getName());
    } else {
      dockerClient.pauseContainer(instance);
      instance.suspend();
    }
  }

  @Override
  public void resumeInstance(ServerInstance instance) {
    if (instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.SUSPENDED)) {
      dockerClient.unpauseContainer(instance);
      instance.resume();
    } else {
      log.info("Not resuming instance={} because it is already running", instance.getName());
    }
  }

  @Override
  public void stopInstance(ServerInstance instance) {
    var dockerInstance = getInstance(instance.getName());
    dockerClient.stopInstance(dockerInstance);
  }

  @Override
  public String getTargetHost(ServerInstance instance) {
    if (System.getProperty("containerised") != null) {
      return instance.getName();
    }

    return "127.0.0.1";
  }

  private void startReverseProxy(ServerInstance instance) {
    new Thread(() -> reverseProxyFactory.from(this, instance).start(), "SERVER-" + instance.getName() + "-PROXY-MANAGER").start();
  }

  @SneakyThrows
  private void awaitHealthy(ServerInstance instance) {
    log.info("Awaiting instance={} to be healthy by pinging={}", instance.getName(), getTargetHost(instance) + ":" + instance.getPrivatePort());
    try {
      while (!instance.getStatus(getTargetHost(instance)).equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(Constants.MILLIS_IN_SECONDS);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Instance is healthy: " + instance.getName());
  }

}
