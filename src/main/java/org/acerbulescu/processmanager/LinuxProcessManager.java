package org.acerbulescu.processmanager;

import org.acerbulescu.models.ThreadInstance;

public class LinuxProcessManager implements ProcessManager {
  @Override
  public String getShell() {
    return "/bin/sh";
  }

  @Override
  public void suspendThread(ThreadInstance threadInstance) {

  }

  @Override
  public void resumeThread(ThreadInstance threadInstance) {

  }
}
