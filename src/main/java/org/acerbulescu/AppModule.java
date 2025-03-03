package org.acerbulescu;

import com.google.inject.AbstractModule;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.configurators.ConfigConfigurator;
import org.acerbulescu.instancemanager.DockerInstanceManger;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.processmanager.LinuxProcessManager;
import org.acerbulescu.processmanager.ProcessManager;
import org.acerbulescu.processmanager.WindowsProcessManager;

@Log4j2
public class AppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConfigRepresentation.class).toProvider(ConfigConfigurator.class);
    bind(InstanceManager.class).to(DockerInstanceManger.class);
//    bind(InstanceManager.class).to(ThreadManager.class);

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      log.info("Windows system detected. Injecting Windows process manager");
      bind(ProcessManager.class).to(WindowsProcessManager.class);
    } else {
      log.info("Linux system detected. Injecting Linux process manager");
      bind(ProcessManager.class).to(LinuxProcessManager.class);
    }
  }
}
