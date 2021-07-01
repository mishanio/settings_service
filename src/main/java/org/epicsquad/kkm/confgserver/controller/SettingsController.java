package org.epicsquad.kkm.confgserver.controller;

import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping()
    public PropertiesPropertySource getSettings(@RequestParam String fileName) {
        Properties properties = settingsService.getSettings(fileName);

        return new PropertiesPropertySource(fileName, properties);
    }


    @PostMapping()
    public void update(@RequestParam String fileName, @RequestBody Map<String, Object> settings){
        settingsService.updateSettings(fileName, settings);
    }
}
