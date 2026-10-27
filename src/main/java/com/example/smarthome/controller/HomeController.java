package com.example.smarthome.controller;

import com.example.smarthome.service.DeviceService;
import com.example.smarthome.service.PowerConsumptionService;
import com.example.smarthome.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final DeviceService deviceService;
    private final RoomService roomService;
    private final PowerConsumptionService powerConsumptionService;

    public HomeController(DeviceService deviceService,
                          RoomService roomService,
                          PowerConsumptionService powerConsumptionService) {
        this.deviceService = deviceService;
        this.roomService = roomService;
        this.powerConsumptionService = powerConsumptionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("deviceCount", deviceService.findAll().size());
        model.addAttribute("roomCount", roomService.findAll().size());
        model.addAttribute("initialPower", powerConsumptionService.getCurrentPowerConsumption());
        return "index";
    }
}
