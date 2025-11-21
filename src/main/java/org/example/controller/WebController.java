package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for serving the frontend
 */
@Controller
public class WebController {

    /**
     * Serve the main page
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
