package org.lightchurch.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.lightchurch.domain.Config;
import org.lightchurch.repository.ConfigsRepository;
import org.lightchurch.service.ConfigService;
import org.lightchurch.service.dto.ConfigDTO;
import org.lightchurch.web.rest.errors.BadRequestAlertException;
import org.lightchurch.web.rest.errors.NameAlreadyUsedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * @author tom9b
 * @version 1.0.0
 * @Date 7/12/2021 Date file was Created
 * @package com.kyosk.configsapi.controllers
 * @project configs-api
 */
@RestController
public class ConfigsController {

    private final Logger logger = LoggerFactory.getLogger(ConfigsController.class);
    private final ConfigService configService;
    private final ConfigsRepository configsRepository;

    public ConfigsController(ConfigService configService, ConfigsRepository configsRepository) {
        this.configService = configService;
        this.configsRepository = configsRepository;
    }

    @GetMapping("/configs")
    public ResponseEntity<List<ConfigDTO>> getAllConfigs() {
        return new ResponseEntity<>(configService.getAllConfig(), HttpStatus.OK);
    }

    /**
     * {@code POST /configs } : Creates a new configuration
     * <p>
     * Create a new configuration if it does not exist and the creation object is valid
     *
     * @param configDTO config to be created
     * @return Created Config
     * @throws URISyntaxException       if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the name is already used
     * @
     */
    @PostMapping("/configs")
    public ResponseEntity<Config> createConfig(@Valid @RequestBody ConfigDTO configDTO) throws URISyntaxException {
        logger.info("REST request to create config: {}", configDTO);

        configsRepository
            .findByName(configDTO.getName())
            .ifPresent(
                c -> {
                    throw new NameAlreadyUsedException();
                }
            );
        Config config = configService.createConfig(configDTO);
        return new ResponseEntity<>(config, HttpStatus.CREATED);
    }

    /**
     * {@code PUT /configs/:name} : Updates the "name" Config.
     *
     * @param configDTO the new config detils
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated config.
     * @throws NameAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     */
    @PutMapping("/configs/{name}")
    public ResponseEntity<ConfigDTO> updateConfig(@PathVariable String name, @Valid @RequestBody ConfigDTO configDTO) {
        logger.debug("REST request to update config with name: {}", name);
        Optional<Config> existingConfig = configsRepository.findByName(name);
        //        When updating to an already existing config name
        if (existingConfig.isPresent() && (!name.equalsIgnoreCase(configDTO.getName()))) throw new NameAlreadyUsedException();
        Optional<ConfigDTO> updatedDto = configService.updateConfig(name, configDTO);

        return ResponseUtil.wrapOrNotFound(updatedDto, new HttpHeaders());
    }

    /**
     * {@code DELETE /configs/:name} : delete the "name" Config.
     *
     * @param name the name of the config to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/configs/{name}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String name) {
        logger.debug("REST request to delete config: {}", name);
        configService.deleteConfig(name);
        return ResponseEntity.noContent().headers(new HttpHeaders()).build();
    }

    /**
     * {@code GET /users/:name} : get the "name" config.
     *
     * @param name the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/configs/{name}")
    public ResponseEntity<ConfigDTO> getConfigByName(@PathVariable String name) {
        logger.debug("REST request to delete config: {}", name);
        Optional<ConfigDTO> configByName = configService.getConfigByName(name);

        return ResponseUtil.wrapOrNotFound(configByName, new HttpHeaders());
    }

    /**
     * {@code GET /search/:params} : search for configs meeting criteria "params" .
     *
     * @param params Filter criteria
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body of a List of all configs meeting the criteria or a Empty list
     * @throws JsonProcessingException If request params cannot be serialised
     */
    @GetMapping("/search")
    public ResponseEntity<List<ConfigDTO>> searchConfigs(@RequestParam Map<String, String> params) throws JsonProcessingException {
        logger.info("REST search request params : {}", new ObjectMapper().writeValueAsString(params));
        return new ResponseEntity<>(configService.searchConfigs(params), HttpStatus.OK);
    }
}
