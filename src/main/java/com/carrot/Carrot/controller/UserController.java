package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> esisteUtente() {
        return ResponseEntity.ok(userService.esisteUtente());
    }

    @GetMapping
    public ResponseEntity<User> getUtente() {
        return userService.getUtente().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> salvaUtente(@RequestBody User user) {
        userService.salvaUtente(user);
        return ResponseEntity.ok().build();
    }
}