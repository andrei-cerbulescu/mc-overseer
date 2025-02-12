package org.acerbulescu.processmanager;

import org.acerbulescu.models.ThreadInstance;

public interface ProcessManager {
  String getShell();

  void suspendThread(ThreadInstance threadInstance);

  void resumeThread(ThreadInstance threadInstance);
}
