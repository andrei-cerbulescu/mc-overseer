package org.acerbulescu.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigRepresentation {
  @Nullable
  private String dockerNetwork;

  @JsonSetter(nulls = Nulls.FAIL)
  private List<ServerInstanceConfigRepresentation> instances;
}
