package org.acerbulescu.configurators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;

import java.io.File;

@Log4j2
public class ConfigConfigurator implements Provider<ConfigRepresentation> {
    private static final String CONFIG_PATH = "./config.json";

    @Override
    public ConfigRepresentation get() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(CONFIG_PATH), ConfigRepresentation.class);
        } catch (Exception e) {
            log.error("Error when reading config file: ", e);
            throw new RuntimeException("Failed to load configuration: " + CONFIG_PATH, e);
        }
    }
}
