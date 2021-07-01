package org.epicsquad.kkm.confgserver.service;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.epicsquad.kkm.confgserver.GitSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsService {

    private final GitSettings gitSettings;
    private final Map<String, File> fileSettingsMap = new ConcurrentHashMap<>();
    String propertiesSuffix = ".properties";
    private final static Logger log = LoggerFactory.getLogger(SettingsService.class);
    private final CredentialsProvider credentialsProvider;

    public SettingsService(GitSettings gitSettings) {
        this.gitSettings = gitSettings;
        credentialsProvider = new UsernamePasswordCredentialsProvider(gitSettings.getLogin(),
                gitSettings.getPassword());
    }

    @PostConstruct
    public void init() throws GitAPIException {
        cloneRepo();
        initSettings();
    }

    public Properties getSettings(String fileName) {
        File file = getSettingsFile(fileName);
        if (!file.exists()) {
            throw new RuntimeException("settingsFile.not.found");
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fileInputStream);
            return props;
        } catch (IOException e) {
            log.error("Exception during reading file: {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    private File getSettingsFile(String fileName) {
        String baseDir = gitSettings.getBaseDir().getAbsolutePath();
        String environment = gitSettings.getEnvironment();
        return new File(baseDir + File.separator + environment + File.separator + fileName + propertiesSuffix);
    }

    private void cloneRepo() throws GitAPIException {
        File baseDir = gitSettings.getBaseDir();
        assert (baseDir.isDirectory());
        deleteBaseDirIfExists(baseDir);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(gitSettings.getUri())
                .setDirectory(baseDir)
                .setBranch(gitSettings.getBranch());
        cloneCommand.call();
    }

    private void initSettings() {
        File baseDir = gitSettings.getBaseDir();
        String environment = gitSettings.getEnvironment();
        Optional<File> maybeDirWithSettings = Arrays.stream(Objects.requireNonNull(baseDir.listFiles()))
                .filter(f -> f.isDirectory() && f.getName().equals(environment))
                .findFirst();
        if (maybeDirWithSettings.isEmpty()) {
            log.error("not found directory with settings for {} environment {}", baseDir, environment);
        } else {
            initSettings(baseDir);
        }
    }

    private void initSettings(File baseDir) {
        for (File file : Objects.requireNonNull(baseDir.listFiles())) {
            if (file.isDirectory()) {
                initSettings(file);
            } else {
                if (file.getName().endsWith(propertiesSuffix)) {
                    String absolutePath = file.getAbsolutePath();
                    log.debug("reading settings from file: {}", absolutePath);
                    String fileName = absolutePath.substring(0, absolutePath.indexOf(propertiesSuffix));
                    fileSettingsMap.put(fileName, file);
                }
            }
        }
    }


    private void deleteBaseDirIfExists(File baseDir) {
        if (baseDir.exists()) {
            for (File file : Objects.requireNonNull(baseDir.listFiles())) {
                try {
                    FileUtils.delete(file, FileUtils.RECURSIVE);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to initialize base directory",
                            e);
                }
            }
        }
    }


    public void updateSettings(String fileName, Map<String, Object> changedSettings) {

        try {
            Properties settings = getSettings(fileName);
            for (Map.Entry<String, Object> entry : changedSettings.entrySet()) {
                settings.put(entry.getKey(), entry.getValue());
            }
            File settingsFile = getSettingsFile(fileName);
            settings.store(new FileOutputStream(settingsFile), null);
            Git git = Git.open(gitSettings.getBaseDir());
            git.add().addFilepattern(gitSettings.getEnvironment() + "/" + fileName)
                    .call();
            git.commit().setAll(true).setMessage("updating file " + fileName)
                    .setAuthor("author", "author@email.com")
                    .call();

            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName(gitSettings.getBranch());
            remoteAddCommand.setUri(new URIish(gitSettings.getUri()));
            remoteAddCommand.call();

            PushCommand pushCommand = git.push();
            pushCommand.setCredentialsProvider(credentialsProvider);
            Iterable<PushResult> results = pushCommand.call();
            results.forEach(result -> result.getRemoteUpdates()
                    .forEach(update -> log.info("update {}", update.getStatus())));


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
