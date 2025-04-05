package org.acerbulescu.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRepresentation {
  private List<ServerInstanceConfigRepresentation> instances;
  private String dockerNetwork;
}
