package org.acerbulescu.instancemanager;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.docker.DockerClient;
import org.acerbulescu.models.DockerInstance;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;
import org.apache.logging.log4j.core.util.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
@AllArgsConstructor
public class DockerInstanceManger implements InstanceManager {
  ReverseProxyFactory reverseProxyFactory;

  DockerClient dockerClient;

  List<DockerInstance> instances = Collections.synchronizedList(new ArrayList<>());

  @Override
  public DockerInstance getInstance(String name) {
    return instances.stream()
        .filter(e -> e.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }

  @Override
  public List<ServerInstance> getInstances() {
    return instances.stream()
        .map(e -> (ServerInstance) e)
        .collect(Collectors.toList());
  }

  @Override
  @PreDestroy
  public void shutdownAllInstances() {
    log.info("Shutting down all instances");
    instances.forEach(e -> stopInstance(e.getName()));
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
  public void suspendInstance(String instanceName) {
    var instance = getInstance(instanceName);
    if (instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      log.info("Not suspending instance={} because it is already suspended", instance.getName());
    } else {
      dockerClient.pauseContainer(instance);
      instance.suspend();
    }
  }

  @Override
  public void resumeInstance(String instanceName) {
    var instance = getInstance(instanceName);
    if (instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      dockerClient.unpauseContainer(instance);
      instance.resume();
    } else {
      log.info("Not resuming instance={} because it is already running", instance.getName());
    }
  }

  @Override
  public void stopInstance(String instanceName) {
    var dockerInstance = getInstance(instanceName);
    dockerClient.stopInstance(dockerInstance);
  }

  @Override
  public void incrementConnectedPlayers(String instanceName) {
    getInstance(instanceName).incrementConnectedPlayers();
  }

  @Override
  public void decrementConnectedPlayers(String instanceName) {
    getInstance(instanceName).decrementConnectedPlayers();
  }

  @Override
  public Integer getConnectedPlayers(String instanceName) {
    return getInstance(instanceName).getConnectedPlayers();
  }

  @Override
  public ServerInstance.Status getInstanceStatus(String instanceName) {
    return getInstance(instanceName).getStatus();
  }

  @Override
  public String getTargetHost(String instanceName) {
    var instance = getInstance(instanceName);
    if (System.getProperty("containerised") != null) {
      return instance.getName();
    }

    return "127.0.0.1";
  }

  @Override
  public String getTargetHost(ServerInstanceConfigRepresentation instance) {
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
    log.info("Awaiting instance={} to be healthy by pinging={}", instance.getName(), getTargetHost(instance.getName()) + ":" + instance.getPrivatePort());
    try {
      while (!instance.getStatus().equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(Constants.MILLIS_IN_SECONDS);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Instance is healthy: " + instance.getName());
  }

}
