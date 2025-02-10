package org.acerbulescu.processmanager;

import org.acerbulescu.models.ThreadInstance;

public interface ProcessManager {
  public String getShell();

  public void suspendThread(ThreadInstance threadInstance);

  public void resumeThread(ThreadInstance threadInstance);
}
