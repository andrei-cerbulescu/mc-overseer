package org.acerbulescu.services;

import java.util.stream.Collectors;

import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.models.ServerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ScheduledServices {
  @Autowired
  InstanceManager instanceManager;

  @Scheduled(fixedDelay = 2 * 60 * 1000)
  public void temporarilyResumeServers() {
    log.info("Running scheduled task: resuming and suspending all instances to prevent failures");
    var instances = instanceManager.getInstancesNames().stream()
        .filter(e -> !instanceManager.getInstanceStatus(e).equals(ServerInstance.Status.SHUTDOWN))
        .collect(Collectors.toList());

    instances.forEach(e -> instanceManager.resumeInstance(e));

    try {
      Thread.sleep(15 * 1000);
    } catch (InterruptedException ignored) {
    }

    instances.forEach(e -> instanceManager.attemptSuspend(e));
  }

  @Scheduled(fixedDelay = 60 * 1000)
  public void restartCrashedServers() {
    log.info("Running scheduled task: restarting crashed instances");
    instanceManager.getInstancesNames()
    .stream()
    .filter(e-> !instanceManager.getInstanceStatus(e).equals(ServerInstance.Status.SHUTDOWN))
    .forEach(e -> instanceManager.restartInstanceIfCrashed(e));
  }
}
