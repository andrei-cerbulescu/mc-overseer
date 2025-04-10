package org.acerbulescu.resources;

import org.acerbulescu.models.ServerInstanceRepresentation;
import org.acerbulescu.services.InstanceServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InstancesResources {
  @Autowired
  private InstanceServices instanceService;

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @GetMapping("/instances")
  public Map<String, List<ServerInstanceRepresentation>> getInstances() {
    return Map.of("instances", instanceService.getInstances());
  }

  @DeleteMapping("/instance/{name}")
  public ResponseEntity stopInstance(@PathVariable String name) {
    instanceService.stopInstance(name);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/instance/{name}")
  public ResponseEntity restartExistingInstance(@PathVariable String name) {
    instanceService.restartExistingInstance(name);
    return ResponseEntity.noContent().build();
  }
}
