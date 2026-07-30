package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Habilita el soporte de Mockito en JUnit para pruebas unitarias rapidas.
@ExtendWith(MockitoExtension.class)
class SpotPersistenceAdapterTest {

    // @Mock: Genera mocks de las dependencias que requiere el adaptador.
    @Mock
    private SpotRepository spotRepository;

    @Mock
    private SpotPersistenceMapper spotPersistenceMapper;

    // @InjectMocks: Crea la instancia de la clase bajo prueba e inyecta automaticamente los mocks anteriores.
    @InjectMocks
    private SpotPersistenceAdapter spotPersistenceAdapter;

    @Test
    @DisplayName("Plaza nueva: debe mapearla a entidad, persistirla y retornarla convertida a dominio de nuevo")
    void shouldSaveNewSpotSuccessfully() {
        // QUE HACE:
        // - Instancia una plaza (Spot) de dominio que NO existe todavia en BD.
        // - Simula una entidad de persistencia y la plaza de retorno mapeada.
        // - Configura los mocks del mapper y del repositorio.
        // - Ejecuta el metodo save del adaptador.
        UUID id = UUID.randomUUID();
        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        SpotEntity entity = new SpotEntity();
        entity.setId(id);
        entity.setType(VehicleType.CAR);
        entity.setStatus(SpotStatus.AVAILABLE);

        when(spotRepository.findById(id)).thenReturn(Optional.empty());
        when(spotPersistenceMapper.toEntity(spot)).thenReturn(entity);
        when(spotRepository.save(entity)).thenReturn(entity);
        when(spotPersistenceMapper.toDomain(entity)).thenReturn(spot);

        Spot result = spotPersistenceAdapter.save(spot);

        // QUE DEBERIA HACER:
        // Debe retornar la plaza persistida correctamente mapeada de vuelta y verificar que se
        // llamo exactamente una vez a los metodos del mapper y del repositorio.
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        verify(spotPersistenceMapper, times(1)).toEntity(spot);
        verify(spotRepository, times(1)).save(entity);
        verify(spotPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Plaza existente: debe mutar la entidad gestionada y NO llamar a save() con una copia remapeada")
    void shouldUpdateManagedEntity_WhenSpotAlreadyExists() {
        // QUE HACE:
        // - Simula una plaza que ya esta en BD y llega modificada desde el dominio.
        // Es el caso de release, update y updateStatus: todos hacen findById antes
        // de guardar, asi que la entidad ya esta GESTIONADA en el contexto.
        UUID id = UUID.randomUUID();
        Spot liberada = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        SpotEntity gestionada = new SpotEntity();
        gestionada.setId(id);
        gestionada.setType(VehicleType.CAR);
        gestionada.setStatus(SpotStatus.OCCUPIED);

        when(spotRepository.findById(id)).thenReturn(Optional.of(gestionada));
        when(spotPersistenceMapper.toDomain(gestionada)).thenReturn(liberada);

        Spot result = spotPersistenceAdapter.save(liberada);

        // QUE DEBERIA HACER:
        // Debe aplicar el cambio sobre la instancia gestionada y dejar que el dirty
        // checking emita el UPDATE al commit. NO debe pasar por toEntity()/save():
        // esa copia lleva version=null, isNew() la daria por nueva y el persist()
        // resultante revienta con NonUniqueObjectException.
        assertThat(gestionada.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
        assertThat(result).isEqualTo(liberada);
        verify(spotRepository, never()).save(any(SpotEntity.class));
        verify(spotPersistenceMapper, never()).toEntity(any(Spot.class));
    }

    @Test
    @DisplayName("Debe retornar un Optional con la plaza si el ID existe en BD")
    void shouldFindSpotByIdSuccessfully() {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Simula que el repositorio encuentra la entidad con ese ID.
        // - Configura el mapper.
        // - Ejecuta la busqueda findById.
        UUID id = UUID.randomUUID();
        SpotEntity entity = new SpotEntity();
        entity.setId(id);
        entity.setType(VehicleType.CAR);
        entity.setStatus(SpotStatus.AVAILABLE);

        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotRepository.findById(id)).thenReturn(Optional.of(entity));
        when(spotPersistenceMapper.toDomain(entity)).thenReturn(spot);

        Optional<Spot> result = spotPersistenceAdapter.findById(id);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con la plaza de dominio correspondiente.
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(spotRepository, times(1)).findById(id);
        verify(spotPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar por ID si no existe en BD")
    void shouldReturnEmptyOptionalWhenIdNotFound() {
        // QUE HACE:
        // - Configura el mock del repositorio para retornar un Optional vacio.
        // - Invoca findById.
        UUID id = UUID.randomUUID();
        when(spotRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Spot> result = spotPersistenceAdapter.findById(id);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio y asegurar que nunca se llamo al mapper.
        assertThat(result).isEmpty();
        verify(spotRepository, times(1)).findById(id);
        verify(spotPersistenceMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar la lista completa de plazas convertida a objetos de dominio")
    void shouldFindAllSpots() {
        // QUE HACE:
        // - Prepara una lista de entidades en base de datos.
        // - Configura los mocks para retornar las entidades y mapear cada una de ellas a dominio.
        // - Llama al metodo findAll.
        SpotEntity entity1 = new SpotEntity();
        entity1.setId(UUID.randomUUID());
        SpotEntity entity2 = new SpotEntity();
        entity2.setId(UUID.randomUUID());

        Spot domain1 = Spot.reconstruct(entity1.getId(), VehicleType.CAR, SpotStatus.AVAILABLE);
        Spot domain2 = Spot.reconstruct(entity2.getId(), VehicleType.MOTORBIKE, SpotStatus.OCCUPIED);

        when(spotRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(spotPersistenceMapper.toDomain(entity1)).thenReturn(domain1);
        when(spotPersistenceMapper.toDomain(entity2)).thenReturn(domain2);

        List<Spot> result = spotPersistenceAdapter.findAll();

        // QUE DEBERIA HACER:
        // Debe retornar una lista de tamaño 2 y verificar que cada elemento ha sido correctamente traducido al dominio.
        assertThat(result).isNotNull().hasSize(2);
        verify(spotRepository, times(1)).findAll();
        verify(spotPersistenceMapper, times(2)).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar true si el ID ya esta registrado en BD")
    void shouldReturnTrueWhenIdExists() {
        // QUE HACE:
        // - Configura el repositorio para indicar que el ID si existe (true).
        // - Invoca existsById en el adaptador.
        UUID id = UUID.randomUUID();
        when(spotRepository.existsById(id)).thenReturn(true);

        boolean exists = spotPersistenceAdapter.existsById(id);

        // QUE DEBERIA HACER:
        // Debe retornar true.
        assertThat(exists).isTrue();
        verify(spotRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Debe retornar false si el ID no esta registrado en BD")
    void shouldReturnFalseWhenIdDoesNotExist() {
        // QUE HACE:
        // - Configura el repositorio para indicar que el ID no existe (false).
        // - Invoca existsById en el adaptador.
        UUID id = UUID.randomUUID();
        when(spotRepository.existsById(id)).thenReturn(false);

        boolean exists = spotPersistenceAdapter.existsById(id);

        // QUE DEBERIA HACER:
        // Debe retornar false.
        assertThat(exists).isFalse();
        verify(spotRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Debe retornar la cantidad correcta al contar por tipo y estado")
    void shouldCountByTypeAndStatusSuccessfully() {
        // QUE HACE:
        // - Configura el repositorio para retornar un conteo especifico (e.g. 5)
        // - Ejecuta countByTypeAndStatus.
        when(spotRepository.countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE)).thenReturn(5L);

        long count = spotPersistenceAdapter.countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE);

        // QUE DEBERIA HACER:
        // Debe retornar exactamente el mismo conteo entregado por el repositorio.
        assertThat(count).isEqualTo(5L);
        verify(spotRepository, times(1)).countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe retornar el total de plazas de un tipo sea cual sea su estado")
    void shouldCountByTypeSuccessfully() {
        // QUE HACE:
        // - Configura el repositorio para retornar el total del tipo.
        // - Ejecuta countByType.
        when(spotRepository.countByType(VehicleType.CAR)).thenReturn(20L);

        long count = spotPersistenceAdapter.countByType(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe delegar en el repositorio y devolver el mismo total.
        assertThat(count).isEqualTo(20L);
        verify(spotRepository, times(1)).countByType(VehicleType.CAR);
    }
    @Test
    @DisplayName("Debe comprobar la disponibilidad por tipo con un conteo, sin usar la consulta con bloqueo")
    void shouldCheckAvailabilityByTypeWithoutLocking() {
        // QUE HACE:
        // - Configura el repositorio para devolver un conteo mayor que cero de plazas AVAILABLE.
        // - Ejecuta existsAvailableByType.
        when(spotRepository.countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE)).thenReturn(3L);

        boolean result = spotPersistenceAdapter.existsAvailableByType(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe retornar true usando el conteo y nunca invocar findFirstAvailable (SELECT ... FOR UPDATE).
        assertThat(result).isTrue();
        verify(spotRepository, times(1)).countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE);
        verify(spotRepository, never()).findFirstAvailable(any());
    }

    @Test
    @DisplayName("Debe retornar false si no hay ninguna plaza disponible del tipo solicitado")
    void shouldReturnFalseWhenNoAvailableSpotOfType() {
        // QUE HACE:
        // - Configura el repositorio para devolver un conteo de cero plazas AVAILABLE.
        // - Ejecuta existsAvailableByType.
        when(spotRepository.countByTypeAndStatus(VehicleType.MOTORBIKE, SpotStatus.AVAILABLE)).thenReturn(0L);

        boolean result = spotPersistenceAdapter.existsAvailableByType(VehicleType.MOTORBIKE);

        // QUE DEBERIA HACER:
        // Debe retornar false.
        assertThat(result).isFalse();
        verify(spotRepository, times(1)).countByTypeAndStatus(VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe encontrar una plaza disponible, ocuparla y guardarla exitosamente")
    void shouldFindAndOccupyAvailableSpotSuccessfully() {
        // QUE HACE:
        // - Genera un ID aleatorio e instancias de entidad y dominio para plaza disponible y ocupada.
        // - Configura el repositorio para retornar la entidad disponible.
        // - Configura el mapper para las traducciones entidad-dominio.
        // - Configura el repositorio para guardar la entidad ocupada.
        // - Ejecuta el metodo findAndOccupyAvailableSpot.
        UUID id = UUID.randomUUID();

        // La entidad que devuelve findFirstAvailable es la GESTIONADA por el
        // contexto de persistencia (y la que queda bloqueada con
        // PESSIMISTIC_WRITE). Es sobre esta sobre la que hay que aplicar el
        // cambio.
        SpotEntity managedEntity = new SpotEntity();
        managedEntity.setId(id);
        managedEntity.setType(VehicleType.CAR);
        managedEntity.setStatus(SpotStatus.AVAILABLE);
        managedEntity.setVersion(0L);

        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotRepository.findFirstAvailable(VehicleType.CAR)).thenReturn(Optional.of(managedEntity));
        when(spotPersistenceMapper.toDomain(managedEntity)).thenReturn(spot);

        Optional<Spot> result = spotPersistenceAdapter.findAndOccupyAvailableSpot(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Ocupar la plaza mutando la entidad gestionada, y dejar que el dirty
        // checking emita el UPDATE al hacer commit.
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(SpotStatus.OCCUPIED);
        assertThat(managedEntity.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
        verify(spotRepository).findFirstAvailable(VehicleType.CAR);

        // NO debe llamar a save() con una entidad remapeada. El dominio Spot no
        // lleva version, asi que mapper.toEntity() devolveria una instancia
        // nueva con el mismo id y version=null; Spring Data la trataria como
        // nueva (isNew() mira la version) y haria persist(), que revienta con
        // NonUniqueObjectException al haber ya una gestionada con ese id.
        verify(spotRepository, never()).save(any(SpotEntity.class));
        verify(spotPersistenceMapper, never()).toEntity(any(Spot.class));
    }

    @Test
    @DisplayName("Debe retornar vacio si no encuentra plazas disponibles para ocupar")
    void shouldReturnEmptyWhenNoAvailableSpotToOccupy() {
        // QUE HACE:
        // - Configura el mock del repositorio para indicar que no hay plazas libres.
        // - Ejecuta findAndOccupyAvailableSpot.
        when(spotRepository.findFirstAvailable(VehicleType.CAR)).thenReturn(Optional.empty());

        Optional<Spot> result = spotPersistenceAdapter.findAndOccupyAvailableSpot(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio y verificar que nunca se invoco el metodo save en BD.
        assertThat(result).isEmpty();
        verify(spotRepository).findFirstAvailable(VehicleType.CAR);
        verify(spotRepository, never()).save(any());
    }
}
