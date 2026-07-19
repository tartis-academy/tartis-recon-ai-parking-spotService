package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;

import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

 @Bean
    public ReleaseSpotUseCase releaseSpotUseCase(SpotPersistence spotPersistence) {
        return new ReleaseSpotUseCase(spotPersistence);
}

}
