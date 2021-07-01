package org.epicsquad.kkm.confgserver;

import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfig {

    @Bean
    SettingsService settingsService(GitSettings gitSettings){
        return new SettingsService(gitSettings);
    }
}
