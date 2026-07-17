package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-17T09:25:16+0200",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class SpotPersistenceMapperImpl implements SpotPersistenceMapper {

    @Override
    public SpotEntity toEntity(Spot spot) {
        if ( spot == null ) {
            return null;
        }

        SpotEntity spotEntity = new SpotEntity();

        spotEntity.setId( spot.getId() );
        if ( spot.getNumSpot() != null ) {
            spotEntity.setNumSpot( spot.getNumSpot() );
        }
        spotEntity.setStatus( spot.getStatus() );

        return spotEntity;
    }

    @Override
    public Spot toDomain(SpotEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Spot spot = new Spot();

        spot.setId( entity.getId() );

        return spot;
    }
}
