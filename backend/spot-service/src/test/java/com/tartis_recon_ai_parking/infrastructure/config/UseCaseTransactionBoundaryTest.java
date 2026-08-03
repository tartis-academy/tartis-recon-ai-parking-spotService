package com.tartis_recon_ai_parking.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.SpotEventListenerAdapter;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence.SpotPersistenceAdapter;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@DisplayName("Frontera transaccional en la capa de aplicacion")
class UseCaseTransactionBoundaryTest {

    @MockitoBean
    private SpotEventListenerAdapter spotEventListenerAdapter;

    @Autowired
    private OccupySpotUseCase occupySpotUseCase;

    @Autowired
    private ReleaseSpotUseCase releaseSpotUseCase;

    @Autowired
    private SpotPersistenceAdapter spotPersistenceAdapter;

    @Test
    @DisplayName("Los casos de uso deben estar proxiados para que @Transactional se aplique")
    void useCasesShouldBeTransactionalProxies() {
        // QUE HACE:
        // - Recupera del contexto los casos de uso que escriben en base de datos.
        // Se instancian con new() en BeanConfiguration, asi que si Spring no los
        // proxia la anotacion @Transactional no hace nada y el occupy pierde el
        // bloqueo pesimista sin que ningun test con mocks lo detecte.

        // QUE DEBERIA HACER:
        // Ambos beans deben llegar envueltos en un proxy AOP.
        assertThat(AopUtils.isAopProxy(occupySpotUseCase)).isTrue();
        assertThat(AopUtils.isAopProxy(releaseSpotUseCase)).isTrue();
    }

    @Test
    @DisplayName("La operacion atomica de ocupacion debe declarar @Transactional")
    void occupyShouldDeclareTransactional() throws NoSuchMethodException {
        // QUE HACE:
        // Busca la anotacion sobre el metodo de la clase real, no sobre el proxy.
        Method execute = AopUtils.getTargetClass(occupySpotUseCase)
                .getMethod("execute", com.tartis_recon_ai_parking.domain.spot.VehicleType.class);

        // QUE DEBERIA HACER:
        // execute() delimita la transaccion que cubre el SELECT ... FOR UPDATE y el save.
        assertThat(execute.getAnnotation(Transactional.class)).isNotNull();
    }

    @Test
    @DisplayName("El adaptador de persistencia ya no debe delimitar transacciones")
    void persistenceAdapterShouldNotDeclareTransactions() {
        // QUE HACE:
        // Recorre los metodos publicos del adaptador de infraestructura.

        // QUE DEBERIA HACER:
        // Ninguno debe declarar @Transactional: la frontera vive en los casos de uso.
        assertThat(AopUtils.getTargetClass(spotPersistenceAdapter).getMethods())
                .noneMatch(method -> method.isAnnotationPresent(Transactional.class));
    }
}
