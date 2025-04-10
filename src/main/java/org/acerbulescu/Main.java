package org.acerbulescu;

import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.mappers.ServerInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@Log4j2
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "org.acerbulescu")
public class Main implements CommandLineRunner {
  private final ConfigRepresentation config;
  private final InstanceManager instanceManager;

  @Autowired
  public Main(ConfigRepresentation config, InstanceManager instanceManager) {
    this.config = config;
    this.instanceManager = instanceManager;
  }


  public static void main(String[] args) {
    var context = SpringApplication.run(Main.class, args);
    context.registerShutdownHook();
  }

  @Override
  public void run(String... args) {

    config.getInstances().stream()
        .map(e -> (ServerInstanceMapper.INSTANCE.from(e, instanceManager.getTargetHost(e))))
        .forEach(
            instance -> new Thread(() -> instanceManager.startInstance(instance), instance.getName() + "-MAIN-THREAD")
                .start());
  }
}
