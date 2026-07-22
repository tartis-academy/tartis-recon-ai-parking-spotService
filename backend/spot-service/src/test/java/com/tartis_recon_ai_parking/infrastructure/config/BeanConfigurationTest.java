package com.tartis_recon_ai_parking.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;

@DisplayName("Tests para BeanConfiguration")
class BeanConfigurationTest {

    private BeanConfiguration beanConfiguration;
    private SpotPersistence spotPersistence;

    @BeforeEach
    void setUp() {
        beanConfiguration = new BeanConfiguration();
        spotPersistence = mock(SpotPersistence.class);
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean CreateSpotUseCase")
    void shouldCreateCreateSpotUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean CreateSpotUseCase, inyectandole el mock de persistencia.
        CreateSpotUseCase useCase = beanConfiguration.createSpotUseCase(spotPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo, comprobando asi que la instanciacion manual es correcta.
        assertThat(useCase).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean GetSpotUseCase")
    void shouldCreateGetSpotUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean GetSpotUseCase.
        GetSpotUseCase useCase = beanConfiguration.getSpotUseCase(spotPersistence);

        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(useCase).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean UpdateSpotUseCase")
    void shouldCreateUpdateSpotUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean UpdateSpotUseCase.
        UpdateSpotUseCase useCase = beanConfiguration.updateSpotUseCase(spotPersistence);

        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(useCase).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean OccupySpotUseCase")
    void shouldCreateOccupySpotUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean OccupySpotUseCase.
        OccupySpotUseCase useCase = beanConfiguration.occupySpotUseCase(spotPersistence);

        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(useCase).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean ReleaseSpotUseCase")
    void shouldCreateReleaseSpotUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean ReleaseSpotUseCase.
        ReleaseSpotUseCase useCase = beanConfiguration.releaseSpotUseCase(spotPersistence);

        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(useCase).isNotNull();
    }
}
