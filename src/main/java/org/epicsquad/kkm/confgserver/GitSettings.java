package org.epicsquad.kkm.confgserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "setting.config.git")
public class GitSettings {

    /**
     * Uri to connect to git repo
     */
    private String uri;
    /**
     * login to connect to git repo
     */
    private String login;
    /**
     * password to connect to git repo
     */
    private String password;
    /**
     * local git repository
     */
    private String baseDir = "/tmp/_settings";
    /**
     * branch to load settings
     */
    private String branch;

    /**
     * directory to load settings
     */
    private String environment;


    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getUri() {
        return uri;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public File getBaseDir() {
        return new File(baseDir);
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBranch() {
        return branch;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
