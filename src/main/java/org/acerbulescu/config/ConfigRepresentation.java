package org.acerbulescu.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.acerbulescu.models.ServerInstanceRepresentation;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigRepresentation {
  private List<ServerInstanceRepresentation> instances;
  private String dockerNetwork;
}
