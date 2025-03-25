package org.acerbulescu.configurators;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.models.ServerInstanceConfigRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
@Configuration
public class ConfigConfigurator {
  private static final String CONFIG_PATH = "./config";
  private static final String CONFIG_FILE_PATH = CONFIG_PATH + "/config.json";

  @Bean
  public ConfigRepresentation configRepresentation() {
    if (System.getProperty("containerised") != null) {
      log.info("Application running inside a container. Behaviour will be adjusted accordingly.");
    }

    var objectMapper = new ObjectMapper();
    var file = new File(CONFIG_FILE_PATH);
    ConfigRepresentation config;
    try {
      if (!file.exists()) {
        log.info("Config file nof found. Creating default");
        return createDefaultFile(file, objectMapper);
      }
      config = objectMapper.readValue(file, ConfigRepresentation.class);
    } catch (Exception e) {
      log.error("Error when reading config file={}", CONFIG_FILE_PATH, e);
      throw new RuntimeException("Failed to load configuration: " + CONFIG_FILE_PATH, e);
    }

    if (StringUtil.isNullOrEmpty(config.getDockerNetwork()) && System.getProperty("containerised") != null) {
      throw new RuntimeException("Network needs to be provided when running in a container and must be the same as host.");
    }

    return config;
  }

  private ConfigRepresentation createDefaultFile(File file, ObjectMapper mapper) {
    var instanceRepresentation = ServerInstanceConfigRepresentation.builder()
        .name("example")
        .publicPort(25566L)
        .privatePort(25565L)
        .path("./servers/example")
        .startCommand("java -Xmx1024M -Xms1024M -jar server.jar nogui")
        .build();

    var defaultConfig = ConfigRepresentation.builder()
        .instances(List.of(instanceRepresentation))
        .dockerNetwork("mcdockerseer")
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
