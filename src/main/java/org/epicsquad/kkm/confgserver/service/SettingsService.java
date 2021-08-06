package org.epicsquad.kkm.confgserver.service;

import org.epicsquad.kkm.confgserver.model.HierarchyPropertySource;
import org.epicsquad.kkm.confgserver.model.SettingsUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SettingsService {

    private final FileProviderRepository fileProviderRepository;
    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String SETTINGS_FOLDER = "settings";
    private final static String IMPORT_PROPERTIES = "_imports";
    private final static Logger log = LoggerFactory.getLogger(SettingsService.class);


    public SettingsService(FileProviderRepository fileProviderRepository) {
        this.fileProviderRepository = fileProviderRepository;
    }

    public HierarchyPropertySource getPropertySourceHierarchy(String fileName) {
        Properties settings = getSettings(fileName);
        List<HierarchyPropertySource> importedSources =
                Arrays.stream(settings.getProperty(IMPORT_PROPERTIES, "").split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(this::getPropertySourceHierarchy)
                        .collect(Collectors.toList());
        return new HierarchyPropertySource(fileName, settings, importedSources);
    }

    public Properties getSettings(String fileName) {
        File file = fileProviderRepository.getFile(composeFilePath(fileName));
        if (!file.exists()) {
            throw new RuntimeException(fileName + " not found");
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

    public void updateSettings(String fileName, SettingsUpdateCommand settingsUpdateCommand) {
        Properties settings = getSettings(fileName);
        Map<String, Object> changedSettings = settingsUpdateCommand.getChangedSettings();
        settings.putAll(changedSettings);
        String filePath = composeFilePath(fileName);
        File settingsFile = fileProviderRepository.getFile(filePath);
        try {
            settings.store(new FileOutputStream(settingsFile), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileProviderRepository.saveFile(filePath, settingsUpdateCommand.getCommitInfo());
    }

    private String composeFilePath(String fileName) {
        return SETTINGS_FOLDER + File.separator + fileName + PROPERTIES_SUFFIX;
    }
}
