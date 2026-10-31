package com.example.smarthome.repository;

import com.example.smarthome.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByNameIgnoreCase(String name);
}
