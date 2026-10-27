package com.example.smarthome.dto;

import java.time.Instant;
import java.util.List;

public class PowerConsumptionDto {
    private double totalPowerW;
    private double totalPowerKw;
    private String formattedPower;
    private int totalDevicesCount;
    private int activeDevicesCount;
    private int unratedActiveCount;
    private int ratedActiveCount;
    private Double estimatedEnergyKwh; // Estimated energy based on recorded telemetry or active simulated duration
    private String energyNote;
    private Instant timestamp;
    private List<ActiveDevicePowerDto> activeDevices;

    public PowerConsumptionDto() {}

    public PowerConsumptionDto(double totalPowerW,
                               int totalDevicesCount,
                               int activeDevicesCount,
                               int unratedActiveCount,
                               int ratedActiveCount,
                               Double estimatedEnergyKwh,
                               String energyNote,
                               Instant timestamp,
                               List<ActiveDevicePowerDto> activeDevices) {
        this.totalPowerW = Math.round(totalPowerW * 100.0) / 100.0;
        this.totalPowerKw = Math.round((totalPowerW / 1000.0) * 1000.0) / 1000.0;
        if (this.totalPowerW >= 1000.0) {
            this.formattedPower = String.format("%.2f kW", this.totalPowerKw);
        } else {
            this.formattedPower = String.format("%.1f W", this.totalPowerW);
        }
        this.totalDevicesCount = totalDevicesCount;
        this.activeDevicesCount = activeDevicesCount;
        this.unratedActiveCount = unratedActiveCount;
        this.ratedActiveCount = ratedActiveCount;
        this.estimatedEnergyKwh = estimatedEnergyKwh != null ? Math.round(estimatedEnergyKwh * 1000.0) / 1000.0 : null;
        this.energyNote = energyNote;
        this.timestamp = timestamp;
        this.activeDevices = activeDevices;
    }

    public double getTotalPowerW() { return totalPowerW; }
    public void setTotalPowerW(double totalPowerW) { this.totalPowerW = totalPowerW; }

    public double getTotalPowerKw() { return totalPowerKw; }
    public void setTotalPowerKw(double totalPowerKw) { this.totalPowerKw = totalPowerKw; }

    public String getFormattedPower() { return formattedPower; }
    public void setFormattedPower(String formattedPower) { this.formattedPower = formattedPower; }

    public int getTotalDevicesCount() { return totalDevicesCount; }
    public void setTotalDevicesCount(int totalDevicesCount) { this.totalDevicesCount = totalDevicesCount; }

    public int getActiveDevicesCount() { return activeDevicesCount; }
    public void setActiveDevicesCount(int activeDevicesCount) { this.activeDevicesCount = activeDevicesCount; }

    public int getUnratedActiveCount() { return unratedActiveCount; }
    public void setUnratedActiveCount(int unratedActiveCount) { this.unratedActiveCount = unratedActiveCount; }

    public int getRatedActiveCount() { return ratedActiveCount; }
    public void setRatedActiveCount(int ratedActiveCount) { this.ratedActiveCount = ratedActiveCount; }

    public Double getEstimatedEnergyKwh() { return estimatedEnergyKwh; }
    public void setEstimatedEnergyKwh(Double estimatedEnergyKwh) { this.estimatedEnergyKwh = estimatedEnergyKwh; }

    public String getEnergyNote() { return energyNote; }
    public void setEnergyNote(String energyNote) { this.energyNote = energyNote; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<ActiveDevicePowerDto> getActiveDevices() { return activeDevices; }
    public void setActiveDevices(List<ActiveDevicePowerDto> activeDevices) { this.activeDevices = activeDevices; }

    public static class ActiveDevicePowerDto {
        private Long id;
        private String name;
        private String roomName;
        private String deviceType;
        private Double ratedPowerW;
        private boolean isRated;

        public ActiveDevicePowerDto() {}

        public ActiveDevicePowerDto(Long id, String name, String roomName, String deviceType, Double ratedPowerW, boolean isRated) {
            this.id = id;
            this.name = name;
            this.roomName = roomName;
            this.deviceType = deviceType;
            this.ratedPowerW = ratedPowerW;
            this.isRated = isRated;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getRoomName() { return roomName; }
        public String getDeviceType() { return deviceType; }
        public Double getRatedPowerW() { return ratedPowerW; }
        public boolean isRated() { return isRated; }
    }
}
