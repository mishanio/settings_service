package org.epicsquad.kkm.confgserver.model;

public class CommitInfo {
    private String reason;
    private String principal;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
