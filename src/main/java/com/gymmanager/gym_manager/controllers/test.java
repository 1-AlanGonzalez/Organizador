package com.gymmanager.gym_manager.controllers;

import org.springframework.web.bind.annotation.GetMapping;

public class test {
@GetMapping("/test")
public String test() {
    return "inicio";
}
}
