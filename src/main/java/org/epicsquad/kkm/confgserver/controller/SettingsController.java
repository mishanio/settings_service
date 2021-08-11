package org.epicsquad.kkm.confgserver.controller;

import org.epicsquad.kkm.confgserver.model.HierarchyPropertySource;
import org.epicsquad.kkm.confgserver.model.SettingsUpdateCommand;
import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping("api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/all")
    public List<PropertiesPropertySource> getAllSettings() {
        return settingsService.getAllSetting();
    }

    @GetMapping("/descriptions")
    public Properties getSettingsDescriptions() {
        return settingsService.getSettingsDescriptions();
    }

    @GetMapping()
    public HierarchyPropertySource getSettings(@RequestParam String fileName) {
        return settingsService.getPropertySourceHierarchy(fileName);
    }

    @PostMapping()
    public void update(@RequestParam String fileName, @RequestBody SettingsUpdateCommand settingsUpdateCommand) {
        settingsService.updateSettings(fileName, settingsUpdateCommand);
    }
}
