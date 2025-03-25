package org.acerbulescu.resources;

import java.util.List;
import java.util.Map;

import org.acerbulescu.models.ServerInstanceRepresentation;
import org.acerbulescu.services.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InstancesResources {
  @Autowired
  private InstanceService instanceService;

  @GetMapping("/instances")
  public ResponseEntity<Map<String, List<ServerInstanceRepresentation>>> getInstances() {
    return ResponseEntity.ok(
        Map.of("instances", instanceService.getInstances()));
  }
}
