package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateSpotUseCase createSpotUseCase(SpotPersistence spotPersistence) {
        return new CreateSpotUseCase(spotPersistence);
    }

    @Bean
    public GetSpotUseCase getSpotUseCase(SpotPersistence spotPersistence) {
        return new GetSpotUseCase(spotPersistence);
    }

    @Bean
    public UpdateSpotUseCase updateSpotUseCase(SpotPersistence spotPersistence) {
        return new UpdateSpotUseCase(spotPersistence);
    }
}
