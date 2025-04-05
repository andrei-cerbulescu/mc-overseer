package org.acerbulescu.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.acerbulescu.instancemanager.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class WebsocketService {
  @Autowired
  private InstanceManager instanceManager;


}
