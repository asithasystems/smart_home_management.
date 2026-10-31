package com.example.smarthome.controller;

import com.example.smarthome.entity.CommunicationStatus;
import com.example.smarthome.entity.Device;
import com.example.smarthome.entity.DeviceType;
import com.example.smarthome.entity.Room;
import com.example.smarthome.service.DeviceService;
import com.example.smarthome.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DeviceController {
    private final DeviceService deviceService;
    private final RoomService roomService;

    public DeviceController(DeviceService deviceService, RoomService roomService) {
        this.deviceService = deviceService;
        this.roomService = roomService;
    }

    @GetMapping("/devices")
    public String devices(Model model) {
        model.addAttribute("devices", deviceService.findAll());
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("device", new Device());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("statuses", CommunicationStatus.values());
        return "devices";
    }

    @PostMapping("/devices")
    public String createDevice(@Valid @ModelAttribute("device") Device device,
                               BindingResult bindingResult,
                               @RequestParam(value = "room.id", required = false) Long roomId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Room room = (roomId != null) ? roomService.findById(roomId) : null;
        if (room == null) {
            bindingResult.rejectValue("room", "room.invalid", "Please select a valid room.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("devices", deviceService.findAll());
            model.addAttribute("rooms", roomService.findAll());
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("statuses", CommunicationStatus.values());
            return "devices";
        }
        deviceService.createDevice(device, room);
        redirectAttributes.addFlashAttribute("successMessage", "Device added successfully.");
        return "redirect:/devices";
    }

    @PostMapping("/devices/{id}/delete")
    public String deleteDevice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Device device = deviceService.findById(id);
        if (device != null) {
            deviceService.deleteDevice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Device '" + device.getName() + "' deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Device not found.");
        }
        return "redirect:/devices";
    }

    @PostMapping("/devices/{id}/toggle")
    public String toggleDevice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Device device = deviceService.toggleDevice(id);
        if (device != null) {
            redirectAttributes.addFlashAttribute("successMessage", device.getName() + " switched " + device.getRequestedState() + ".");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Device not found.");
        }
        return "redirect:/devices";
    }

    @GetMapping("/devices/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Device device = deviceService.findById(id);
        if (device == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Device not found.");
            return "redirect:/devices";
        }
        model.addAttribute("device", device);
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("statuses", CommunicationStatus.values());
        return "edit-device";
    }

    @PostMapping("/devices/{id}/edit")
    public String updateDevice(@PathVariable Long id,
                               @Valid @ModelAttribute("device") Device formDevice,
                               BindingResult bindingResult,
                               @RequestParam(value = "room.id", required = false) Long roomId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Device existing = deviceService.findById(id);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Device not found.");
            return "redirect:/devices";
        }
        Room room = (roomId != null) ? roomService.findById(roomId) : null;
        if (room == null) {
            bindingResult.rejectValue("room", "room.invalid", "Please select a valid room.");
        }

        if (bindingResult.hasErrors()) {
            formDevice.setRoom(room);
            formDevice.setRequestedState(existing.getRequestedState());
            formDevice.setActualState(existing.getActualState());
            model.addAttribute("rooms", roomService.findAll());
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("statuses", CommunicationStatus.values());
            return "edit-device";
        }

        deviceService.updateDevice(id, formDevice, room);
        redirectAttributes.addFlashAttribute("successMessage", "Device updated successfully.");
        return "redirect:/devices";
    }
}
