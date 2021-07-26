package org.epicsquad.kkm.confgserver.controller;

import org.epicsquad.kkm.confgserver.model.HierarchyPropertySource;
import org.epicsquad.kkm.confgserver.model.SettingsUpdateCommand;
import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
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
