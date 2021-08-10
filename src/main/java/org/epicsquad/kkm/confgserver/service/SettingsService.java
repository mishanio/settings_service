package org.epicsquad.kkm.confgserver.service;

import org.epicsquad.kkm.confgserver.model.HierarchyPropertySource;
import org.epicsquad.kkm.confgserver.model.SettingsUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
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
        Properties settings = getSettings(composeFilePath(fileName));
        List<HierarchyPropertySource> importedSources =
                Arrays.stream(settings.getProperty(IMPORT_PROPERTIES, "").split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(this::getPropertySourceHierarchy)
                        .collect(Collectors.toList());
        return new HierarchyPropertySource(fileName, settings, importedSources);
    }

    public List<PropertiesPropertySource> getAllSetting() {
        return fileProviderRepository.listFiles(SETTINGS_FOLDER).stream()
                .flatMap(f -> getAllSettingRecursive(f).stream())
                .collect(Collectors.toList());
    }

    public void updateSettings(String fileName, SettingsUpdateCommand settingsUpdateCommand) {
        Properties settings = getSettings(composeFilePath(fileName));
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

    private Properties getSettings(String fileName) {
        File file = fileProviderRepository.getFile(fileName);
        if (!file.exists()) {
            throw new RuntimeException(fileName + " not found");
        }
        return loadProperties(file);
    }

    private List<PropertiesPropertySource> getAllSettingRecursive(File file) {
        List<PropertiesPropertySource> sources = new ArrayList<>();
        if (file.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .forEach(f -> sources.addAll(getAllSettingRecursive(f)));
        } else {
            String fileName = stripFilePath(file.getAbsolutePath());
            Properties settings = loadProperties(file);
            sources.add(new PropertiesPropertySource(fileName, settings));
        }
        return sources;
    }

    private String composeFilePath(String fileName) {
        return SETTINGS_FOLDER + File.separator + fileName + PROPERTIES_SUFFIX;
    }

    private String stripFilePath(String absoluteFileName) {
        int stripIndex = absoluteFileName.lastIndexOf(SETTINGS_FOLDER) + SETTINGS_FOLDER.length() + "/".length();
        int endIndex = absoluteFileName.endsWith(PROPERTIES_SUFFIX) ? absoluteFileName.indexOf(PROPERTIES_SUFFIX) :
                absoluteFileName.length();
        return absoluteFileName.substring(stripIndex, endIndex);
    }

    private Properties loadProperties(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fileInputStream);
            return props;
        } catch (IOException e) {
            log.error("Exception during reading file: {}", file);
            throw new RuntimeException(e);
        }
    }


}
