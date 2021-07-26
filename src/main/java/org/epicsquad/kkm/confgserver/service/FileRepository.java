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
import org.epicsquad.kkm.confgserver.model.CommitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileRepository {
    private final static Logger log = LoggerFactory.getLogger(FileRepository.class);

    private final GitSettings gitSettings;
    private final CredentialsProvider credentialsProvider;

    public FileRepository(GitSettings gitSettings) {
        this.gitSettings = gitSettings;
        credentialsProvider = new UsernamePasswordCredentialsProvider(gitSettings.getLogin(),
                gitSettings.getPassword());
    }

    public void init() throws GitAPIException {
        cloneRepo();
    }

    public File getFile(String fileName) {
        String baseDir = gitSettings.getBaseDir().getAbsolutePath();
        String environment = gitSettings.getEnvironment();
        return new File(baseDir + File.separator + environment + File.separator + fileName);
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

    public void saveFile(String filePath, CommitInfo commitInfo) {
        try {
            Git git = Git.open(gitSettings.getBaseDir());
            git.add()
                    .addFilepattern(gitSettings.getEnvironment() + "/" + filePath)
                    .call();
            git.commit()
//                    .setAll(true)
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
}
