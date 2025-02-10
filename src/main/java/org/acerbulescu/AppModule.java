package org.acerbulescu;

import com.google.inject.AbstractModule;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.configurators.ConfigConfigurator;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.instancemanager.ThreadManager;
import org.acerbulescu.processmanager.LinuxProcessManager;
import org.acerbulescu.processmanager.ProcessManager;
import org.acerbulescu.processmanager.WindowsProcessManager;

public class AppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConfigRepresentation.class).toProvider(ConfigConfigurator.class);
    bind(InstanceManager.class).to(ThreadManager.class);

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      bind(ProcessManager.class).to(WindowsProcessManager.class);
    } else {
      bind(ProcessManager.class).to(LinuxProcessManager.class);
    }
  }
}
