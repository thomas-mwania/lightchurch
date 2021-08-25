package org.lightchurch.repository;

import java.util.List;
import java.util.Optional;
import org.lightchurch.domain.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author tom9b
 * @version 1.0.0
 * @Date 7/12/2021 Date file was Created
 * @package com.kyosk.configsapi.repositories
 * @project configs-api
 * <p>
 * * Spring Data JPA repository for the {@link Config} entity.
 */
@Repository
public interface ConfigsRepository extends JpaRepository<Config, String> {
    Optional<Config> findByName(String name);

    Optional<List<Config>> findByMetaDataContains(String regex);
}
