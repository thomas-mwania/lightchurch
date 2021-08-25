package org.lightchurch.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author tom9b
 * @version 1.0.0
 * @Date 7/12/2021 Date file was Created
 * @package com.kyosk.configsapi.entities
 * @project configs-api
 * <p>
 * A configuration
 */
@Entity
@Table(name = "configs")
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "metadata")
    private String metaData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }
}
