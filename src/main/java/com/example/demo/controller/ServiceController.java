package com.example.demo.controller;

import com.example.demo.model.UtilityService;
import com.example.demo.service.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceManager serviceManager;

    @GetMapping
    public String viewServices(Model model) {
        model.addAttribute("services", serviceManager.getAllServices());
        return "services"; // Trỏ tới file services.html
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("service", new UtilityService());
        return "service-form"; // Trỏ tới file service-form.html
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute("service") UtilityService service) {
        serviceManager.saveService(service);
        return "redirect:/services";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        serviceManager.deleteService(id);
        return "redirect:/services";
    }
}