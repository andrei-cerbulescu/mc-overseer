package org.acerbulescu.mappers;

import org.acerbulescu.models.ServerInstance;
import org.acerbulescu.models.ServerInstanceRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServerInstanceMapper {
  ServerInstanceMapper INSTANCE = Mappers.getMapper(ServerInstanceMapper.class);

  ServerInstance from(ServerInstanceRepresentation representation);
}
