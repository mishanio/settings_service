package org.epicsquad.kkm.confgserver.service;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.epicsquad.kkm.confgserver.GitSettings;
import org.epicsquad.kkm.confgserver.model.CommitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileProviderRepository {
    private final static Logger log = LoggerFactory.getLogger(FileProviderRepository.class);

    private final GitSettings gitSettings;
    private final CredentialsProvider credentialsProvider;

    public FileProviderRepository(GitSettings gitSettings) {
        this.gitSettings = gitSettings;
        credentialsProvider = new UsernamePasswordCredentialsProvider(gitSettings.getLogin(),
                gitSettings.getPassword());
    }

    public void init() throws GitAPIException {
        cloneRepo();
    }

    public File getFile(String fileName) {
        pull();
        String baseDir = gitSettings.getBaseDir().getAbsolutePath();
        String environment = gitSettings.getEnvironment();
        return new File(baseDir + File.separator + environment + File.separator + fileName);
    }

    public List<File> listFiles(String directoryName) {
        pull();
        String baseDir = gitSettings.getBaseDir().getAbsolutePath();
        String environment = gitSettings.getEnvironment();
        File folder = new File(baseDir + File.separator + environment + File.separator + directoryName);
        if (!folder.isDirectory()) {
            throw new RuntimeException("file is not a directory");
        }
        return List.of(Objects.requireNonNull(folder.listFiles()));
    }

    public void saveFile(String filePath, CommitInfo commitInfo) {
        pull();
        try {
            Git git = Git.open(gitSettings.getBaseDir());
            git.add()
                    .addFilepattern(gitSettings.getEnvironment() + "/" + filePath)
                    .call();
            git.commit()
                    .setMessage(commitInfo.getReason())
                    .setAuthor(commitInfo.getPrincipal(), commitInfo.getPrincipal())
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

    private void pull() {
        try {
            Git git = Git.open(gitSettings.getBaseDir());
            PullCommand pullCmd = git.pull()
                    .setCredentialsProvider(credentialsProvider);
            pullCmd.call();
        } catch (IOException | GitAPIException e) {
            log.error("error during pull command", e);
        }
    }


    private void cloneRepo() throws GitAPIException {
        File baseDir = gitSettings.getBaseDir();
        assert (baseDir.isDirectory());
        deleteBaseDirIfExists(baseDir);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setURI(gitSettings.getUri())
                .setDirectory(baseDir)
                .setBranch(gitSettings.getBranch());
        cloneCommand.call();
    }

    private void deleteBaseDirIfExists(File baseDir) {
        if (baseDir.exists()) {
            for (File file : Objects.requireNonNull(baseDir.listFiles())) {
                try {
                    FileUtils.delete(file, FileUtils.RECURSIVE);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to initialize base directory", e);
                }
            }
        }
    }

}
