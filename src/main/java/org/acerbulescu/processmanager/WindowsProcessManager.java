package org.acerbulescu.processmanager;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.ThreadInstance;

import java.io.IOException;

@Log4j2
public class WindowsProcessManager implements ProcessManager {
  @Override
  public String getShell() {
    return "cmd.exe";
  }

  @Override
  public void suspendThread(ThreadInstance threadInstance) {
    log.info("Suspending instance: " + threadInstance.getInstance().getName());
    try {
      new ProcessBuilder("powershell", "-Command",
          "Stop-Process -Id " + getPid(threadInstance) + " -Suspend").start();
    } catch (IOException e) {
      log.error("Could not suspend instance: " + threadInstance.getInstance().getName());
    }
  }

  @Override
  public void resumeThread(ThreadInstance threadInstance) {
    log.info("Resuming instance: " + threadInstance.getInstance().getName());
    try {
      new ProcessBuilder("powershell", "-Command",
          "Resume-Process -Id " + getPid(threadInstance)).start();
    } catch (IOException e) {
      log.error("Could not resume instance: " + threadInstance.getInstance().getName());
    }
  }

  private static Long getPid(ThreadInstance threadInstance) {
    return threadInstance.getThread().getId();
  }
}
