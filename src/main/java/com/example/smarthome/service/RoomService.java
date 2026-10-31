package com.example.smarthome.service;

import com.example.smarthome.entity.Room;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;

    public RoomService(RoomRepository roomRepository, DeviceRepository deviceRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public boolean existsByNameIgnoreCase(String name) {
        return roomRepository.existsByNameIgnoreCase(name);
    }

    public long getDeviceCountForRoom(Long roomId) {
        return deviceRepository.countByRoomId(roomId);
    }

    @Transactional
    public Room createRoom(Room room) {
        room.setName(room.getName().trim());
        return roomRepository.save(room);
    }

    /**
     * Deletes a room if it exists and has no devices assigned.
     * Throws IllegalStateException if the room still has assigned devices.
     */
    @Transactional
    public boolean deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            return false;
        }
        long deviceCount = deviceRepository.countByRoomId(id);
        if (deviceCount > 0) {
            throw new IllegalStateException("Room cannot be deleted because it still contains " + deviceCount + " device(s). Please delete or reassign them first.");
        }
        roomRepository.deleteById(id);
        return true;
    }
}
