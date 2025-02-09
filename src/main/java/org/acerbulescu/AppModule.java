package org.acerbulescu;

import com.google.inject.AbstractModule;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.configurators.ConfigConfigurator;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.instancemanager.ThreadManager;
import org.acerbulescu.reverseproxy.ReverseProxyFactory;

public class AppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConfigRepresentation.class).toProvider(ConfigConfigurator.class);
    bind(InstanceManager.class).to(ThreadManager.class);
  }
}
