package com.example.smarthome.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_readings")
public class TelemetryReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reading_device"))
    private Device device;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double voltageV;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double currentA;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double powerW;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean simulated = true;

    public TelemetryReading() {}

    public Long getId() { return id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public Double getVoltageV() { return voltageV; }
    public void setVoltageV(Double voltageV) { this.voltageV = voltageV; }
    public Double getCurrentA() { return currentA; }
    public void setCurrentA(Double currentA) { this.currentA = currentA; }
    public Double getPowerW() { return powerW; }
    public void setPowerW(Double powerW) { this.powerW = powerW; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }
}
