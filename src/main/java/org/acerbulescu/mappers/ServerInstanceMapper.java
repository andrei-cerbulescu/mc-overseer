package org.acerbulescu.mappers;

import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;
import org.acerbulescu.models.ServerInstanceRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServerInstanceMapper {
  ServerInstanceMapper INSTANCE = Mappers.getMapper(ServerInstanceMapper.class);

  @Mapping(target = "status", defaultValue = "unhealthy", ignore = true)
  ServerInstance from(ServerInstanceConfigRepresentation representation, String host);

  ServerInstanceRepresentation from(ServerInstance instance);
}
