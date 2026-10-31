package com.example.smarthome;

import com.example.smarthome.dto.RegistrationDto;
import com.example.smarthome.entity.CommunicationStatus;
import com.example.smarthome.entity.Device;
import com.example.smarthome.entity.DeviceType;
import com.example.smarthome.entity.Room;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.RoomRepository;
import com.example.smarthome.service.DeviceService;
import com.example.smarthome.service.RoomService;
import com.example.smarthome.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class SmartHomeCrudSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void cleanUp() {
        deviceRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void unauthenticatedCrudIsBlocked() throws Exception {
        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .param("name", "Bedroom"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("name", "Desk Lamp"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "asitha", roles = {"USER"})
    void authenticatedUserCanPerformRoomAndDeviceCrud() throws Exception {
        // 1. Create Room
        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .param("name", "Living Room")
                        .param("description", "Main Lounge"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"))
                .andExpect(flash().attribute("successMessage", "Room added successfully."));

        Room createdRoom = roomRepository.findAll().stream()
                .filter(r -> "Living Room".equalsIgnoreCase(r.getName()))
                .findFirst()
                .orElse(null);
        assertThat(createdRoom).isNotNull();

        // 2. Create Device
        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("name", "Smart Bulb")
                        .param("deviceType", DeviceType.LIGHT.name())
                        .param("room.id", String.valueOf(createdRoom.getId()))
                        .param("ratedPowerW", "9.5")
                        .param("requestedState", "OFF")
                        .param("communicationStatus", CommunicationStatus.ONLINE.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"))
                .andExpect(flash().attribute("successMessage", "Device added successfully."));

        Device createdDevice = deviceRepository.findAll().stream()
                .filter(d -> d.getName().equals("Smart Bulb"))
                .findFirst()
                .orElse(null);
        assertThat(createdDevice).isNotNull();
        assertThat(createdDevice.getActualState()).isEqualTo("OFF");

        // 3. Toggle Device
        mockMvc.perform(post("/devices/" + createdDevice.getId() + "/toggle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));

        Device toggledDevice = deviceService.findById(createdDevice.getId());
        assertThat(toggledDevice.getActualState()).isEqualTo("ON");

        // 4. Delete Device
        mockMvc.perform(post("/devices/" + createdDevice.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));

        assertThat(deviceService.findById(createdDevice.getId())).isNull();

        // 5. Delete Room
        mockMvc.perform(post("/rooms/" + createdRoom.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"));

        assertThat(roomService.findById(createdRoom.getId())).isNull();
    }
}
