package org.epicsquad.kkm.confgserver;

import org.epicsquad.kkm.confgserver.service.FileProviderRepository;
import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfig {



    @Bean(initMethod = "init")
    FileProviderRepository fileRepository(GitSettings gitSettings){
        return new FileProviderRepository(gitSettings);
    }

    @Bean
    SettingsService settingsService(FileProviderRepository fileProviderRepository){
        return new SettingsService(fileProviderRepository);
    }
}
