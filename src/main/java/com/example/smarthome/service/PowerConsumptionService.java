package com.example.smarthome.service;

import com.example.smarthome.dto.PowerConsumptionDto;
import com.example.smarthome.entity.Device;
import com.example.smarthome.entity.TelemetryReading;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.TelemetryReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PowerConsumptionService {

    private final DeviceRepository deviceRepository;
    private final TelemetryReadingRepository telemetryReadingRepository;

    public PowerConsumptionService(DeviceRepository deviceRepository,
                                   TelemetryReadingRepository telemetryReadingRepository) {
        this.deviceRepository = deviceRepository;
        this.telemetryReadingRepository = telemetryReadingRepository;
    }

    /**
     * Calculates the instantaneous combined power consumption of all ON devices in read-only mode.
     * Prevents duplicate counting by iterating unique device entities.
     * OFF devices contribute strictly 0.0 W.
     * Devices with null or non-positive wattage are flagged as unrated (0.0 W contribution with transparent explanation).
     */
    @Transactional(readOnly = true)
    public PowerConsumptionDto getCurrentPowerConsumption() {
        List<Device> allDevices = deviceRepository.findAll();

        double totalPowerW = 0.0;
        int activeCount = 0;
        int unratedActiveCount = 0;
        int ratedActiveCount = 0;

        List<PowerConsumptionDto.ActiveDevicePowerDto> activeList = new ArrayList<>();

        // Ensure we count each distinct device once
        Set<Long> processedDeviceIds = new HashSet<>();

        for (Device device : allDevices) {
            if (device.getId() != null && !processedDeviceIds.add(device.getId())) {
                continue; // Avoid duplicate devices
            }

            boolean isDeviceOn = "ON".equalsIgnoreCase(device.getActualState()) ||
                    "ON".equalsIgnoreCase(device.getRequestedState());

            if (isDeviceOn) {
                activeCount++;
                Double ratedW = device.getRatedPowerW();
                boolean isRated = ratedW != null && ratedW > 0.0;

                if (isRated) {
                    totalPowerW += ratedW;
                    ratedActiveCount++;
                } else {
                    unratedActiveCount++;
                }

                String roomName = (device.getRoom() != null) ? device.getRoom().getName() : "Unassigned Room";
                String typeName = (device.getDeviceType() != null) ? device.getDeviceType().name() : "UNKNOWN";

                activeList.add(new PowerConsumptionDto.ActiveDevicePowerDto(
                        device.getId(),
                        device.getName(),
                        roomName,
                        typeName,
                        isRated ? ratedW : null,
                        isRated
                ));
            }
        }

        // Energy (kWh) calculation:
        // Check if there is actual telemetry history in telemetry_readings.
        // If not, calculate estimated consumption based on current active load since device last state change (updatedAt)
        Double estimatedEnergyKwh = null;
        String energyNote;

        List<TelemetryReading> telemetryList = telemetryReadingRepository.findAll();
        if (!telemetryList.isEmpty()) {
            // If historical telemetry records exist, aggregate energy using trapezoidal or sampled integration
            // Energy (kWh) = sum(powerW * deltaHours / 1000)
            telemetryList.sort(Comparator.comparing(TelemetryReading::getRecordedAt));
            double totalWattHours = 0.0;
            for (int i = 0; i < telemetryList.size() - 1; i++) {
                TelemetryReading r1 = telemetryList.get(i);
                TelemetryReading r2 = telemetryList.get(i + 1);
                long seconds = Duration.between(r1.getRecordedAt(), r2.getRecordedAt()).getSeconds();
                if (seconds > 0 && seconds <= 3600) { // skip large gaps
                    double avgPower = (r1.getPowerW() + r2.getPowerW()) / 2.0;
                    totalWattHours += (avgPower * (seconds / 3600.0));
                }
            }
            estimatedEnergyKwh = totalWattHours / 1000.0;
            energyNote = "Estimated from simulated telemetry log history (" + telemetryList.size() + " samples)";
        } else {
            // Telemetry table has 0 rows:
            // Calculate active estimated energy from device updatedAt timestamp for currently ON devices
            double accumulatedWattHours = 0.0;
            LocalDateTime now = LocalDateTime.now();
            for (Device device : allDevices) {
                boolean isDeviceOn = "ON".equalsIgnoreCase(device.getActualState()) ||
                        "ON".equalsIgnoreCase(device.getRequestedState());
                if (isDeviceOn && device.getRatedPowerW() != null && device.getRatedPowerW() > 0.0) {
                    LocalDateTime stateStart = device.getUpdatedAt() != null ? device.getUpdatedAt() : device.getCreatedAt();
                    if (stateStart != null && stateStart.isBefore(now)) {
                        long activeSeconds = Duration.between(stateStart, now).getSeconds();
                        // Cap sensible session window to 24h to avoid exaggerated figures
                        if (activeSeconds > 86400) {
                            activeSeconds = 86400;
                        }
                        accumulatedWattHours += (device.getRatedPowerW() * (activeSeconds / 3600.0));
                    }
                }
            }
            estimatedEnergyKwh = accumulatedWattHours / 1000.0;
            energyNote = "Estimated from active duration since last switch event (simulation)";
        }

        return new PowerConsumptionDto(
                totalPowerW,
                allDevices.size(),
                activeCount,
                unratedActiveCount,
                ratedActiveCount,
                estimatedEnergyKwh,
                energyNote,
                Instant.now(),
                activeList
        );
    }
}
