package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sensors", schema = "public")
@Data
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;
}
