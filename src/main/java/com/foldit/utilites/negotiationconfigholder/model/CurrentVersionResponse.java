package com.foldit.utilites.negotiationconfigholder.model;

public class CurrentVersionResponse {
    private String latestVersion;
    private boolean mandatoryUpgrade;

    public CurrentVersionResponse(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public boolean isMandatoryUpgrade() {
        return mandatoryUpgrade;
    }

    public void setMandatoryUpgrade(boolean mandatoryUpgrade) {
        this.mandatoryUpgrade = mandatoryUpgrade;
    }
}
