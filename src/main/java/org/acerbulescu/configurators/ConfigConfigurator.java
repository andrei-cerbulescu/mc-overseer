package org.acerbulescu.configurators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.models.ServerInstanceRepresentation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class ConfigConfigurator implements Provider<ConfigRepresentation> {
  private static final String CONFIG_PATH = "./config";
  private static final String CONFIG_FILE_PATH = CONFIG_PATH + "/config.json";

  @Override
  public ConfigRepresentation get() {
    var objectMapper = new ObjectMapper();
    var file = new File(CONFIG_FILE_PATH);
    try {
      if (!file.exists()) {
        log.info("Config file nof found. Creating default");
        return createDefaultFile(file, objectMapper);
      }
      return objectMapper.readValue(file, ConfigRepresentation.class);
    } catch (Exception e) {
      log.error("Error when reading config file: ", e);
      throw new RuntimeException("Failed to load configuration: " + CONFIG_FILE_PATH, e);
    }
  }

  private ConfigRepresentation createDefaultFile(File file, ObjectMapper mapper) {
    var instanceRepresentation = ServerInstanceRepresentation.builder()
        .name("example")
        .publicPort(25566L)
        .privatePort(25565L)
        .path("./servers/example")
        .startCommand("java -Xmx1024M -Xms1024M -jar server.jar nogui")
        .build();

    var defaultConfig = ConfigRepresentation.builder()
        .instances(List.of(instanceRepresentation))
        .build();

    try {
      Files.createDirectories(Paths.get(CONFIG_PATH));
      mapper.writerWithDefaultPrettyPrinter().writeValue(file, defaultConfig);
      log.info("Default config created at: " + file.getAbsolutePath());
    } catch (Exception e) {
      log.error("Failed to create default config: " + e.getMessage());
      throw new RuntimeException("Default config could not be created. Exiting...");
    }

    return defaultConfig;
  }
}
