package org.acerbulescu.instancemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.acerbulescu.docker.DockerClient;
import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;
import org.acerbulescu.reverseproxy.ReverseProxy;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;
import org.apache.logging.log4j.core.util.Constants;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@AllArgsConstructor
public class DockerInstanceManger implements InstanceManager {
  ReverseProxyFactory reverseProxyFactory;

  DockerClient dockerClient;

  List<ServerInstance> instances = Collections.synchronizedList(new ArrayList<>());

  @Override
  public ServerInstance getInstance(String name) {
    return instances.stream()
        .filter(e -> e.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Instance could not be found"));
  }


  @Override
  public List<ServerInstance> getInstances() {
    return instances;
  }

  @Override
  public List<String> getInstancesNames() {
    return instances.stream()
        .map(ServerInstance::getName)
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

    awaitHealthy(containerInstance);

    startReverseProxies(containerInstance);
  }

  @Override
  public void startExistingInstance(String instanceName) {
    var instance = getInstance(instanceName);
    if (instance.getStatus().equals(ServerInstance.Status.SHUTDOWN)) {
      instance.start();
    }

    createContainer(instance);
    awaitHealthy(instance);

    startReverseProxies(instance);
  }

  @Override
  public void restartInstanceIfCrashed(String instanceName) {
    if (dockerClient.containerExists("/" + instanceName)) {
      return;
    }

    log.info("Recovering instance={} from crash", instanceName);
    var instance = getInstance(instanceName);
    instance.getReverseProxies().forEach(ReverseProxy::stop);

    createContainer(instance);
    awaitHealthy(instance);

    startReverseProxies(instance);
  }

  ServerInstance createContainer(ServerInstance instance) {
    dockerClient.createServerContainer(instance);
    dockerClient.startContainer(instance);

    return instance;
  }

  private void suspendInstance(String instanceName) {
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
    var ServerInstance = getInstance(instanceName);
    var reverseProxies = ServerInstance.getReverseProxies();
    reverseProxies.forEach(e -> {
      if (e != null) {
        e.stop();
      }
    });

    ServerInstance.getReverseProxies().clear();

    dockerClient.stopInstance(ServerInstance);
    ServerInstance.stop();
  }

  @Override
  public synchronized void attemptSuspend(String instanceName) {
    var instance = getInstance(instanceName);

    if (instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      log.info("Not suspending instance={} due to already being suspended", instance.getName());
      return;
    }

    if (instance.getStaticConnectionsCounter().get() != 0) {
      log.info("Not suspending instance={} due to existing static connections", instance.getName());
      return;
    }

    var allIdle = instance.getReverseProxies().stream()
        .map(ReverseProxy::getStatus)
        .allMatch(e -> e.equals(ReverseProxy.Status.IDLE));

    if (!allIdle) {
      log.info("Not suspending instance={} due to existing proxy connections", instance.getName());
      return;
    }

    log.info("Suspending instance={}", instance.getName());
    suspendInstance(instanceName);
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

  @Override
  public void incrementStaticConnections(String instanceName) {
    var instance = getInstance(instanceName);
    if (instance.getStatus().equals(ServerInstance.Status.SUSPENDED)) {
      resumeInstance(instanceName);
    }

    if (instance.getStatus().equals(ServerInstance.Status.SHUTDOWN)) {
      log.error("Instance is shutdown. Cannot increment static connections");
      throw new RuntimeException("Instance is shutdown. Cannot increment static connections");
    }

    getInstance(instanceName).getStaticConnectionsCounter().incrementAndGet();
  }

  @Override
  public void decrementStaticConnections(String instanceName) {
    getInstance(instanceName).getStaticConnectionsCounter().decrementAndGet();
    attemptSuspend(instanceName);
  }

  private void startReverseProxies(ServerInstance instance) {
    var reverseProxy = reverseProxyFactory.from(this, instance.getName(), instance.getPublicPort(),
        instance.getPrivatePort(), ReverseProxyFactory.Protocol.TCP);
    instance.getReverseProxies().add(reverseProxy);

    new Thread(reverseProxy::start, "SERVER-" + instance.getName() + "-MAIN-PROXY").start();

    instance.getPorts().forEach(e -> {
      var proxy = reverseProxyFactory.from(this, instance.getName(), e.getPublicPort(),
          e.getPrivatePort(), e.getProtocol());

      instance.getReverseProxies().add(proxy);

      new Thread(proxy::start, "SERVER-" + instance.getName() + "-PROXY-" + e.getPublicPort()).start();
    });
  }

  @SneakyThrows
  private void awaitHealthy(ServerInstance instance) {
    log.info("Awaiting instance={} to be healthy by pinging={}", instance.getName(), getTargetHost(instance.getName()) + ":" + instance.getPrivatePort());
    try {
      while (!instance.getStatus().equals(ServerInstance.Status.HEALTHY)) {
        Thread.sleep(Constants.MILLIS_IN_SECONDS);
      }
    } catch (Exception e) {
      log.error("Could not await healthy for instance={}", instance.getName(), e);
    }
    log.info("Instance={} is healthy", instance.getName());
  }

}
