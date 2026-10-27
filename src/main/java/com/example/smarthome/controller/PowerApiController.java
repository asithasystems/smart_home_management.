package com.example.smarthome.controller;

import com.example.smarthome.dto.PowerConsumptionDto;
import com.example.smarthome.service.PowerConsumptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/power")
public class PowerApiController {

    private final PowerConsumptionService powerConsumptionService;

    public PowerApiController(PowerConsumptionService powerConsumptionService) {
        this.powerConsumptionService = powerConsumptionService;
    }

    /**
     * Protected lightweight endpoint returning real-time simulated instantaneous power and telemetry estimates.
     * Accessible only to authenticated users via Spring Security.
     * Performs zero database writes.
     */
    @GetMapping("/live")
    public ResponseEntity<PowerConsumptionDto> getLivePowerConsumption() {
        PowerConsumptionDto data = powerConsumptionService.getCurrentPowerConsumption();
        return ResponseEntity.ok(data);
    }
}
