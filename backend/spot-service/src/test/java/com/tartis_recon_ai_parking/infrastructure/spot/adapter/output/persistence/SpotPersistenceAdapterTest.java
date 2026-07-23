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
    @DisplayName("Debe guardar una plaza mapeandola a entidad y retornandola convertida a dominio de nuevo")
    void shouldSaveSpotSuccessfully() {
        // QUE HACE:
        // - Instancia una plaza (Spot) de dominio.
        // - Simula una entidad de persistencia y la plaza de retorno mapeada.
        // - Configura los mocks del mapper y del repositorio.
        // - Ejecuta el metodo save del adaptador.
        UUID id = UUID.randomUUID();
        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        SpotEntity entity = new SpotEntity();
        entity.setId(id);
        entity.setType(VehicleType.CAR);
        entity.setStatus(SpotStatus.AVAILABLE);

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
    @DisplayName("Debe encontrar una plaza disponible, ocuparla y guardarla exitosamente")
    void shouldFindAndOccupyAvailableSpotSuccessfully() {
        // QUE HACE:
        // - Genera un ID aleatorio e instancias de entidad y dominio para plaza disponible y ocupada.
        // - Configura el repositorio para retornar la entidad disponible.
        // - Configura el mapper para las traducciones entidad-dominio.
        // - Configura el repositorio para guardar la entidad ocupada.
        // - Ejecuta el metodo findAndOccupyAvailableSpot.
        UUID id = UUID.randomUUID();
        SpotEntity availableEntity = new SpotEntity();
        availableEntity.setId(id);
        availableEntity.setType(VehicleType.CAR);
        availableEntity.setStatus(SpotStatus.AVAILABLE);

        Spot availableSpot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotEntity occupiedEntity = new SpotEntity();
        occupiedEntity.setId(id);
        occupiedEntity.setType(VehicleType.CAR);
        occupiedEntity.setStatus(SpotStatus.OCCUPIED);
        
        Spot occupiedSpot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(spotRepository.findFirstAvailable(VehicleType.CAR)).thenReturn(Optional.of(availableEntity));
        when(spotPersistenceMapper.toDomain(availableEntity)).thenReturn(availableSpot);
        when(spotPersistenceMapper.toEntity(any(Spot.class))).thenReturn(occupiedEntity);
        when(spotRepository.save(occupiedEntity)).thenReturn(occupiedEntity);
        when(spotPersistenceMapper.toDomain(occupiedEntity)).thenReturn(occupiedSpot);

        Optional<Spot> result = spotPersistenceAdapter.findAndOccupyAvailableSpot(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con la plaza modificada a estado OCCUPIED.
        // Debe asegurar que se llamo al metodo de busqueda y guardado.
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(SpotStatus.OCCUPIED);
        verify(spotRepository).findFirstAvailable(VehicleType.CAR);
        verify(spotRepository).save(occupiedEntity);
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
