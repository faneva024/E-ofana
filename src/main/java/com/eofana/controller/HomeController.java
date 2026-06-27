package com.eofana.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Mande ny back nay , teste ftsn ty fa afaka fafana ny HomeController.java  avieo a.";
    }
}
