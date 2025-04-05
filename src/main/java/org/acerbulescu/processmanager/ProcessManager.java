package org.acerbulescu.processmanager;

import org.acerbulescu.models.ThreadInstance;

public interface ProcessManager {

  void suspendThread(ThreadInstance threadInstance);

  void resumeThread(ThreadInstance threadInstance);
}
