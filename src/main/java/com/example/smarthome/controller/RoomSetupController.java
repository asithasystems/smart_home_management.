package com.example.smarthome.controller;

import com.example.smarthome.entity.Room;
import com.example.smarthome.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RoomSetupController {
    private final RoomService roomService;

    public RoomSetupController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        List<Room> rooms = roomService.findAll();
        Map<Long, Long> roomDeviceCounts = new HashMap<>();
        for (Room r : rooms) {
            roomDeviceCounts.put(r.getId(), roomService.getDeviceCountForRoom(r.getId()));
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomDeviceCounts", roomDeviceCounts);
        model.addAttribute("room", new Room());
        return "rooms";
    }

    @PostMapping("/rooms")
    public String createRoom(@Valid @ModelAttribute("room") Room room,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (room.getName() != null && roomService.existsByNameIgnoreCase(room.getName().trim())) {
            bindingResult.rejectValue("name", "room.duplicate", "A room with this name already exists.");
        }
        if (bindingResult.hasErrors()) {
            List<Room> rooms = roomService.findAll();
            Map<Long, Long> roomDeviceCounts = new HashMap<>();
            for (Room r : rooms) {
                roomDeviceCounts.put(r.getId(), roomService.getDeviceCountForRoom(r.getId()));
            }
            model.addAttribute("rooms", rooms);
            model.addAttribute("roomDeviceCounts", roomDeviceCounts);
            return "rooms";
        }
        roomService.createRoom(room);
        redirectAttributes.addFlashAttribute("successMessage", "Room added successfully.");
        return "redirect:/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Room room = roomService.findById(id);
        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found.");
            return "redirect:/rooms";
        }

        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room '" + room.getName() + "' deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete room: " + e.getMessage());
        }
        return "redirect:/rooms";
    }
}
