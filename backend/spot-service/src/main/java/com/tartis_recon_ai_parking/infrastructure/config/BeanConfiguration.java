package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
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

    @Bean
    public OccupySpotUseCase occupySpotUseCase(SpotPersistence spotPersistence) {
        return new OccupySpotUseCase(spotPersistence);
    }

    @Bean
    public ReleaseSpotUseCase releaseSpotUseCase(SpotPersistence spotPersistence) {
        return new ReleaseSpotUseCase(spotPersistence);
    }
}
