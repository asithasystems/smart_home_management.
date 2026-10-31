package com.example.smarthome.repository;

import com.example.smarthome.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByRoomId(Long roomId);
    long countByRoomId(Long roomId);
}
