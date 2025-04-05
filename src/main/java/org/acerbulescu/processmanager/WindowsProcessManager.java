package org.acerbulescu.processmanager;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.ThreadInstance;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@AllArgsConstructor
public class WindowsProcessManager implements ProcessManager {
  @Override
  public void suspendThread(ThreadInstance threadInstance) {
    log.info("Suspending instance: " + threadInstance.getName());
    try {
      new ProcessBuilder("powershell", "-Command",
          "Stop-Process -Id " + getPid(threadInstance) + " -Suspend").start();
    } catch (IOException e) {
      log.error("Could not suspend instance: " + threadInstance.getName());
    }
  }

  @Override
  public void resumeThread(ThreadInstance threadInstance) {
    log.info("Resuming instance: " + threadInstance.getName());
    try {
      new ProcessBuilder("powershell", "-Command",
          "Resume-Process -Id " + getPid(threadInstance)).start();
    } catch (IOException e) {
      log.error("Could not resume instance: " + threadInstance.getName());
    }
  }

  private static Long getPid(ThreadInstance threadInstance) {
    return threadInstance.getThread().getId();
  }
}
