package org.lightchurch.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.lightchurch.IntegrationTest;
import org.lightchurch.domain.Config;
import org.lightchurch.repository.ConfigsRepository;
import org.lightchurch.service.ConfigService;
import org.lightchurch.service.dto.ConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@IntegrationTest
class ConfigsControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ConfigsControllerTest.class);
    private static final String DEFAULT_SEARCH_STRING = "metadata.monitoring.enabled=true";
    private static final String DEFAULT_CONFIG_NAME = "test_config";
    private static final String CONFIG_NAME_1 = "data-src";
    private static final String DEFAULT_CONFIG_METADATA =
        "{\"monitoring\":{\"enabled\":\"false\"},\"limits\":{\"cpu\":{\"enabled\":\"false\",\"value\":\"300m\"}}}";
    private static final String DEFAULT_CONFIG_UPDATE_METADATA =
        "{\"monitoring\":{\"enabled\":\"false\"},\"limits\":{\"cpu\":{\"enabled\":\"false\",\"value\":\"400m\"}}}";

    @Autowired
    ConfigsRepository configsRepository;

    @Autowired
    ConfigService configService;

    @Autowired
    private MockMvc restUserMockMvc;

    @Autowired
    private EntityManager em;

    private List<Config> configs;

    public static List<Config> createConfigs(EntityManager em) {
        List<Config> configs = new ArrayList<>();
        Config c = new Config();
        Config c1 = new Config();
        Config c2 = new Config();
        c.setName("data-src");
        c.setMetaData("{\"monitoring\":{\"enabled\":\"true\"},\"limits\":{\"cpu\":{\"enabled\":\"false\",\"value\":\"300m\"}}}");
        c1.setName("data-src2");
        c1.setMetaData("{\"monitoring\":{\"enabled\":\"false\"},\"limits\":{\"cpu\":{\"enabled\":\"true\",\"value\":\"300m\"}}}");
        c2.setName("data-src3");
        c2.setMetaData("{\"monitoring\":{\"enabled\":\"true\"},\"limits\":{\"cpu\":{\"enabled\":\"true\",\"value\":\"250m\"}}}");
        configs.add(c);
        configs.add(c1);
        configs.add(c2);
        return configs;
    }

    @BeforeEach
    void setUp() {
        configs = createConfigs(em);
    }

    @Test
    @Transactional(readOnly = true)
    void getAllConfigs() throws Exception {
        ConfigDTO configDTO = new ConfigDTO();
        int databaseSizeBeforeCreate = configsRepository.findAll().size();
        restUserMockMvc
            .perform(get("/configs").accept(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isOk());
        assertPersistedConfigs(
            cl -> {
                assertThat(cl).hasSize(databaseSizeBeforeCreate);
            }
        );
    }

    @Test
    @Transactional
    void createConfig() throws Exception {
        int databaseSizeBeforeCreate = configsRepository.findAll().size();
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setMetaData(new ObjectMapper().readValue(DEFAULT_CONFIG_METADATA, HashMap.class));
        configDTO.setName(DEFAULT_CONFIG_NAME);

        restUserMockMvc
            .perform(post("/configs").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isCreated());
        assertPersistedConfigs(
            cl -> {
                assertThat(cl).hasSize(databaseSizeBeforeCreate + 1);
                Config testConfig = cl.get(cl.size() - 1);
                assertThat(testConfig.getName()).isEqualTo(DEFAULT_CONFIG_NAME);
                assertThat(testConfig.getMetaData()).isEqualTo(DEFAULT_CONFIG_METADATA);
            }
        );
    }

    @Test
    @Transactional
    void updateConfig() throws Exception {
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setMetaData(new ObjectMapper().readValue(DEFAULT_CONFIG_UPDATE_METADATA, HashMap.class));
        configDTO.setName(CONFIG_NAME_1);

        restUserMockMvc
            .perform(
                put("/configs/{name}", CONFIG_NAME_1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isOk());
        assertPersistedConfig(
            cl -> {
                assertThat(cl).isPresent();
                Config testConfig = cl.get();
                assertThat(testConfig.getName()).isEqualTo(CONFIG_NAME_1);
                assertThat(testConfig.getMetaData()).isEqualTo(DEFAULT_CONFIG_UPDATE_METADATA);
            }
        );
    }

    @Test
    @Transactional
    void deleteConfig() throws Exception {
        configsRepository.saveAll(configs);
        configsRepository.flush();
        int databaseSizeBeforeDelete = configsRepository.findAll().size();

        // Delete the user
        restUserMockMvc
            .perform(delete("/configs/{name}", configs.get(0).getName()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        assertPersistedConfigs(configs -> assertThat(configs).hasSize(databaseSizeBeforeDelete - 1));
    }

    @Test
    @Transactional
    void getConfigByName() throws Exception {
        configsRepository.saveAll(configs);
        configsRepository.flush();
        restUserMockMvc
            .perform(get("/configs/{name}", CONFIG_NAME_1))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.name").value(CONFIG_NAME_1));
    }

    @Test
    @Transactional(readOnly = true)
    void searchConfigs() throws Exception {
        configsRepository.saveAll(configs);
        configsRepository.flush();
        ConfigDTO configDTO = new ConfigDTO();
        MvcResult response = restUserMockMvc
            .perform(
                get("/search?" + DEFAULT_SEARCH_STRING)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
        List<ConfigDTO> configs = new ObjectMapper().readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(configs).hasSize(2);
    }

    private void assertPersistedConfigs(Consumer<List<Config>> configAssertion) {
        configAssertion.accept(configsRepository.findAll());
    }

    private void assertPersistedConfig(Consumer<Optional<Config>> configAssertion) {
        configAssertion.accept(configsRepository.findByName(CONFIG_NAME_1));
    }
}
