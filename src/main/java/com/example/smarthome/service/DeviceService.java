package com.example.smarthome.service;

import com.example.smarthome.entity.Device;
import com.example.smarthome.entity.Room;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.RoomRepository;
import com.example.smarthome.repository.TelemetryReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final TelemetryReadingRepository telemetryReadingRepository;

    public DeviceService(DeviceRepository deviceRepository,
                         TelemetryReadingRepository telemetryReadingRepository) {
        this.deviceRepository = deviceRepository;
        this.telemetryReadingRepository = telemetryReadingRepository;
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Device findById(Long id) {
        return deviceRepository.findById(id).orElse(null);
    }

    @Transactional
    public Device createDevice(Device device, Room room) {
        device.setRoom(room);
        if (device.getRequestedState() == null || device.getRequestedState().isBlank()) {
            device.setRequestedState("OFF");
        }
        if (device.getActualState() == null || device.getActualState().isBlank()) {
            device.setActualState(device.getRequestedState());
        }
        return deviceRepository.save(device);
    }

    @Transactional
    public Device updateDevice(Long id, Device formDevice, Room room) {
        Device existing = deviceRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setName(formDevice.getName());
        existing.setDeviceType(formDevice.getDeviceType());
        existing.setRoom(room);
        existing.setRatedPowerW(formDevice.getRatedPowerW());
        existing.setCommunicationStatus(formDevice.getCommunicationStatus());
        return deviceRepository.save(existing);
    }

    @Transactional
    public boolean deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            return false;
        }
       
        telemetryReadingRepository.deleteByDeviceId(id);
        deviceRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Device toggleDevice(Long id) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            return null;
        }
        String nextState = "ON".equalsIgnoreCase(device.getRequestedState()) ? "OFF" : "ON";
        device.setRequestedState(nextState);
        device.setActualState(nextState);
        return deviceRepository.save(device);
    }
}
