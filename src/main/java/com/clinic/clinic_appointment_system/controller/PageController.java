package com.clinic.clinic_appointment_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/appointments")
    public String viewSlots() {
        return "appointment_slots";
    }

    @GetMapping("/book")
    public String bookAppointment() {
        return "appointment_book";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "appointment_confirmation";
    }
}