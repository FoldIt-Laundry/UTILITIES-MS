package com.foldit.utilites.negotiationconfigholder.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Configuration")
public class Configuration {
    private String id;
    private String configKey;
    private String configValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}


