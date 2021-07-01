package org.epicsquad.kkm.confgserver;

import org.epicsquad.kkm.confgserver.service.FileRepository;
import org.epicsquad.kkm.confgserver.service.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfig {



    @Bean(initMethod = "init")
    FileRepository fileRepository(GitSettings gitSettings){
        return new FileRepository(gitSettings);
    }

    @Bean
    SettingsService settingsService(FileRepository fileRepository){
        return new SettingsService(fileRepository);
    }
}
