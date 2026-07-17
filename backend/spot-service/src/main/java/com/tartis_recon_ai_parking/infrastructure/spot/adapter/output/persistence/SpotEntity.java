package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;


import java.util.UUID;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla de plazas en PostgreSQL (Spot DB).
 * Vive unicamente en infrastructure (IN-32).
 */
@Entity
@Table(name = "spots")
public class SpotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID uniqueid;

    @Column(name = "num_spot", nullable = false)
    private int numSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SpotStatus status;

    protected SpotEntity() {
        // Requerido por JPA/Hibernate.
    }

    public UUID getuniqueId() {
        return uniqueid;
    }

    public void setuniqueId(UUID id) {
        this.uniqueid = id;
    }

    public int getNumSpot() {
        return numSpot;
    }

    public void setNumSpot(int numSpot) {
        this.numSpot = numSpot;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }
}