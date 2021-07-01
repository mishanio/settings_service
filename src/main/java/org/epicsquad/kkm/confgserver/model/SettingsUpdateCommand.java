package org.epicsquad.kkm.confgserver.model;

import java.util.Map;

public class SettingsUpdateCommand {
    private CommitInfo commitInfo;
    private Map<String, Object> changedSettings;


    public CommitInfo getCommitInfo() {
        return commitInfo;
    }

    public void setCommitInfo(CommitInfo commitInfo) {
        this.commitInfo = commitInfo;
    }

    public Map<String, Object> getChangedSettings() {
        return changedSettings;
    }

    public void setChangedSettings(Map<String, Object> changedSettings) {
        this.changedSettings = changedSettings;
    }
}
