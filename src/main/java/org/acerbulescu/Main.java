package org.acerbulescu;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.mappers.ServerInstanceMapper;

@Log4j2
public class Main {

  public static void main(String[] args) {
    var main = new Main();

    main.start();
  }

  public void start() {
    Injector injector = Guice.createInjector(new AppModule());

    var config = injector.getInstance(ConfigRepresentation.class);
    var instanceManager = injector.getInstance(InstanceManager.class);

    Runtime.getRuntime().addShutdownHook(new Thread(instanceManager::shutdownAllInstances, "shutdown-thread"));

    config.getInstances().stream().map(ServerInstanceMapper.INSTANCE::from).forEach(instance -> {
          new Thread(() -> {
            instanceManager.startInstance(instance);
          }).start();
        }
    );


  }
}