package com.drfirst.bblt.session1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the Week 1 presentation UI
 * Provides a comprehensive web interface demonstrating all foundation models integration
 */
@Controller
public class UIController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Week 1 Foundation Models Demo");
        return "index";
    }

    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("title", "Foundation Models Interactive Demo");
        return "index";
    }

    @GetMapping("/presentation")
    public String presentation(Model model) {
        model.addAttribute("title", "Week 1 Presentation - Foundation Models");
        return "index";
    }
}