package com.example.smarthome;

import com.example.smarthome.dto.PowerConsumptionDto;
import com.example.smarthome.entity.CommunicationStatus;
import com.example.smarthome.entity.Device;
import com.example.smarthome.entity.DeviceType;
import com.example.smarthome.entity.Room;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.RoomRepository;
import com.example.smarthome.service.PowerConsumptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class LivePowerConsumptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PowerConsumptionService powerConsumptionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
        roomRepository.deleteAll();

        Room room = new Room();
        room.setName("Living Room");
        room.setDescription("Test Area");
        testRoom = roomRepository.save(room);
    }

    @Test
    void unauthenticatedAccessToPowerApiIsBlocked() throws Exception {
        mockMvc.perform(get("/api/power/live"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "asitha", roles = {"USER"})
    void allDevicesOffContributesZeroWatts() throws Exception {
        Device d1 = new Device();
        d1.setName("Chandelier");
        d1.setRoom(testRoom);
        d1.setDeviceType(DeviceType.LIGHT);
        d1.setRatedPowerW(60.0);
        d1.setRequestedState("OFF");
        d1.setActualState("OFF");
        d1.setCommunicationStatus(CommunicationStatus.ONLINE);
        deviceRepository.save(d1);

        PowerConsumptionDto result = powerConsumptionService.getCurrentPowerConsumption();
        assertThat(result.getTotalPowerW()).isEqualTo(0.0);
        assertThat(result.getActiveDevicesCount()).isEqualTo(0);
        assertThat(result.getTotalDevicesCount()).isEqualTo(1);

        mockMvc.perform(get("/api/power/live"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPowerW").value(0.0))
                .andExpect(jsonPath("$.formattedPower").value("0.0 W"))
                .andExpect(jsonPath("$.activeDevicesCount").value(0));
    }

    @Test
    @WithMockUser(username = "asitha", roles = {"USER"})
    void multipleDevicesOnCalculatesCorrectCombinedWattsWithoutDuplicates() throws Exception {
        Device d1 = new Device();
        d1.setName("Lamp A");
        d1.setRoom(testRoom);
        d1.setDeviceType(DeviceType.LIGHT);
        d1.setRatedPowerW(15.5);
        d1.setRequestedState("ON");
        d1.setActualState("ON");
        d1.setCommunicationStatus(CommunicationStatus.ONLINE);
        deviceRepository.save(d1);

        Device d2 = new Device();
        d2.setName("Heater");
        d2.setRoom(testRoom);
        d2.setDeviceType(DeviceType.THERMOSTAT);
        d2.setRatedPowerW(1200.0);
        d2.setRequestedState("ON");
        d2.setActualState("ON");
        d2.setCommunicationStatus(CommunicationStatus.ONLINE);
        deviceRepository.save(d2);

        Device d3 = new Device();
        d3.setName("Fan (Off)");
        d3.setRoom(testRoom);
        d3.setDeviceType(DeviceType.SWITCH);
        d3.setRatedPowerW(45.0);
        d3.setRequestedState("OFF");
        d3.setActualState("OFF");
        d3.setCommunicationStatus(CommunicationStatus.ONLINE);
        deviceRepository.save(d3);

        PowerConsumptionDto result = powerConsumptionService.getCurrentPowerConsumption();
        // 15.5 + 1200.0 = 1215.5 W (d3 is OFF and contributes 0)
        assertThat(result.getTotalPowerW()).isEqualTo(1215.5);
        assertThat(result.getTotalPowerKw()).isEqualTo(1.216); // 1215.5 / 1000 = 1.2155 -> rounded to 1.216
        assertThat(result.getFormattedPower()).isEqualTo("1.22 kW");
        assertThat(result.getActiveDevicesCount()).isEqualTo(2);
        assertThat(result.getTotalDevicesCount()).isEqualTo(3);
        assertThat(result.getRatedActiveCount()).isEqualTo(2);
        assertThat(result.getUnratedActiveCount()).isEqualTo(0);

        mockMvc.perform(get("/api/power/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPowerW").value(1215.5))
                .andExpect(jsonPath("$.totalPowerKw").value(1.216))
                .andExpect(jsonPath("$.activeDevicesCount").value(2))
                .andExpect(jsonPath("$.totalDevicesCount").value(3));
    }

    @Test
    @WithMockUser(username = "asitha", roles = {"USER"})
    void unratedOrZeroWattDeviceIsReportedTransparently() throws Exception {
        Device d1 = new Device();
        d1.setName("Unrated Sensor");
        d1.setRoom(testRoom);
        d1.setDeviceType(DeviceType.SENSOR);
        d1.setRatedPowerW(null); // unrated
        d1.setRequestedState("ON");
        d1.setActualState("ON");
        d1.setCommunicationStatus(CommunicationStatus.ONLINE);
        deviceRepository.save(d1);

        PowerConsumptionDto result = powerConsumptionService.getCurrentPowerConsumption();
        assertThat(result.getTotalPowerW()).isEqualTo(0.0);
        assertThat(result.getActiveDevicesCount()).isEqualTo(1);
        assertThat(result.getUnratedActiveCount()).isEqualTo(1);
        assertThat(result.getRatedActiveCount()).isEqualTo(0);
        assertThat(result.getActiveDevices().get(0).isRated()).isFalse();

        mockMvc.perform(get("/api/power/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unratedActiveCount").value(1))
                .andExpect(jsonPath("$.activeDevices[0].rated").value(false));
    }
}
