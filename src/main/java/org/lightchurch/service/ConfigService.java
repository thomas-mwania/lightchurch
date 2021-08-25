package org.lightchurch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.lightchurch.domain.Config;
import org.lightchurch.repository.ConfigsRepository;
import org.lightchurch.service.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author tom9b
 * @version 1.0.0
 * @Date 7/12/2021 Date file was Created
 * @package com.kyosk.configsapi.services
 * @project configs-api
 * <p>
 * Service class for managing users.
 */
@Service
@Transactional
public class ConfigService {

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final ConfigsRepository configsRepository;

    public ConfigService(ConfigsRepository configsRepository) {
        this.configsRepository = configsRepository;
    }

    /**
     * @param configDTO details for creating the new config
     * @return new config
     */
    public Config createConfig(ConfigDTO configDTO) {
        Config config = new Config();
        config.setName(configDTO.getName());
        config.setMetaData(new JSONObject(configDTO.getMetaData()).toString());
        configsRepository.save(config);
        logger.debug("Created a configuration as follows: {}", config);
        return config;
    }

    /**
     * Update the a specified config and return details after the update
     *
     * @param configDTO config to update
     * @return updated config
     */
    public Optional<ConfigDTO> updateConfig(String name, ConfigDTO configDTO) {
        return Optional
            .of(configsRepository.findByName(name))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(
                config -> {
                    config.setMetaData(new JSONObject(configDTO.getMetaData()).toString());
                    logger.debug("Updated configs for {} to {}", config.getName(), config.getMetaData());
                    return config;
                }
            )
            .map(ConfigDTO::new);
    }

    /**
     * Delete a config given the name and log the details if debug is enabled
     *
     * @param name name for the config to delete
     */
    public void deleteConfig(String name) {
        configsRepository
            .findByName(name)
            .ifPresent(
                config -> {
                    configsRepository.delete(config);
                    logger.debug("Deleted config {}", config);
                }
            );
    }

    /**
     * @return List of all configs
     */
    @Transactional(readOnly = true)
    public List<ConfigDTO> getAllConfig() {
        return configsRepository.findAll().stream().map(ConfigDTO::new).collect(Collectors.toList());
    }

    /**
     * @return Config by name if present
     */
    @Transactional(readOnly = true)
    public Optional<ConfigDTO> getConfigByName(String name) {
        return configsRepository.findByName(name).map(ConfigDTO::new);
    }

    /**
     * @param params Hashmap specifying the search criteria
     * @return A List of configs meeting criteria
     */
    public List<ConfigDTO> searchConfigs(Map<String, String> params) {
        Map.Entry<String, String> entry = params.entrySet().iterator().next();
        String[] path = entry.getKey().split("\\.");
        String config = path[1];
        String key = path[2];
        String value = entry.getValue();
        Optional<List<Config>> configs = (Optional<List<Config>>) configsRepository.findByMetaDataContains(config);
        List<Config> filtered = new ArrayList<>();
        configs.ifPresent(
            configList -> {
                configList.forEach(
                    c -> {
                        try {
                            Map<String, Object> map = (Map<String, Object>) new ObjectMapper()
                                .readValue(c.getMetaData(), HashMap.class)
                                .get(config);
                            if (map.get(key).equals(value)) filtered.add(c);
                        } catch (JsonProcessingException e) {
                            logger.error(new Object() {}.getClass().getEnclosingMethod().getName(), e);
                        }
                    }
                );
            }
        );
        logger.debug("Returning {} filtered configs", filtered.size());
        return filtered.stream().map(ConfigDTO::new).collect(Collectors.toList());
    }
}
