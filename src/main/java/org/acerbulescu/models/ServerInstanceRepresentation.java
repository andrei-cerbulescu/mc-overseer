package org.acerbulescu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acerbulescu.models.ServerInstance.Status;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerInstanceRepresentation {
  private String name;
  private Integer publicPort;
  private Integer privatePort;
  private String path;
  private String startCommand;
  private Status status;
}
