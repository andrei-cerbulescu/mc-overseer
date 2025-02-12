package org.acerbulescu.processmanager;

import java.io.IOException;

import org.acerbulescu.models.ThreadInstance;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LinuxProcessManager implements ProcessManager {
  @Override
  public String getShell() {
    return "/bin/sh";
  }

  @Override
  public void suspendThread(ThreadInstance threadInstance) {
    log.info("Suspending instance: " + threadInstance.getName());
    try {
      new ProcessBuilder("kill", "-STOP", String.valueOf(getPid(threadInstance))).start();
    } catch (IOException e) {
      log.error("Could not suspend instance: " + threadInstance.getName());
    }
  }

  @Override
  public void resumeThread(ThreadInstance threadInstance) {
    log.info("Resuming instance: " + threadInstance.getName());
    try {
      new ProcessBuilder("kill", "-CONT", String.valueOf(getPid(threadInstance))).start();
    } catch (IOException e) {
      log.error("Could not resume instance: " + threadInstance.getName());
    }
  }

  private static Long getPid(ThreadInstance threadInstance) {
    return threadInstance.getThread().getId();
  }
}
