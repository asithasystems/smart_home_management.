package com.example.smarthome.repository;

import com.example.smarthome.entity.TelemetryReading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {
    List<TelemetryReading> findTop50ByDeviceIdOrderByRecordedAtDesc(Long deviceId);
    void deleteByDeviceId(Long deviceId);
}
