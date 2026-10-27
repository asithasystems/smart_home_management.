package com.example.smarthome.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must be 100 characters or fewer")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Choose a device type")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceType deviceType = DeviceType.LIGHT;

    @NotNull(message = "Choose a room")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_device_room"))
    private Room room;

    @Column(length = 20)
    private String requestedState = "OFF";

    @Column(length = 20)
    private String actualState = "OFF";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunicationStatus communicationStatus = CommunicationStatus.ONLINE;

    @DecimalMin(value = "0.1", message = "Rated power must be greater than zero if specified")
    @Column(nullable = true)
    private Double ratedPowerW = 10.0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Device() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (requestedState == null) requestedState = "OFF";
        if (actualState == null) actualState = "OFF";
        if (communicationStatus == null) communicationStatus = CommunicationStatus.ONLINE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public String getRequestedState() { return requestedState; }
    public void setRequestedState(String requestedState) { this.requestedState = requestedState; }
    public String getActualState() { return actualState; }
    public void setActualState(String actualState) { this.actualState = actualState; }
    public CommunicationStatus getCommunicationStatus() { return communicationStatus; }
    public void setCommunicationStatus(CommunicationStatus communicationStatus) { this.communicationStatus = communicationStatus; }
    public Double getRatedPowerW() { return ratedPowerW; }
    public void setRatedPowerW(Double ratedPowerW) { this.ratedPowerW = ratedPowerW; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
