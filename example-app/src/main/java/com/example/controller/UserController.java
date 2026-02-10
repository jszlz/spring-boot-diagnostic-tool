package com.example.controller;

import com.diagnostic.core.annotation.DiagnosticEndpoint;
import com.example.model.User;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @DiagnosticEndpoint(name = "getAllUsers", slowThresholdMs = 500)
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @DiagnosticEndpoint(name = "getUserById", critical = true)
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @DiagnosticEndpoint(name = "createUser")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @GetMapping("/slow")
    @DiagnosticEndpoint(name = "slowEndpoint", slowThresholdMs = 100)
    public String slowEndpoint() throws InterruptedException {
        Thread.sleep(500); // Simulate slow operation
        return "Slow response";
    }
}
