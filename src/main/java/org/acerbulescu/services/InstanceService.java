package org.acerbulescu.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acerbulescu.instancemanager.InstanceManager;
import org.acerbulescu.mappers.ServerInstanceMapper;
import org.acerbulescu.models.ServerInstanceRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class InstanceService {
  @Autowired
  private InstanceManager instanceManager;

  public List<ServerInstanceRepresentation> getInstances() {
    var instances = instanceManager.getInstances();
    return instances.stream()
        .map(ServerInstanceMapper.INSTANCE::from)
        .collect(Collectors.toList());
  }
}
