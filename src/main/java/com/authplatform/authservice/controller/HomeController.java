package com.authplatform.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from AuthService Backend! (Public API)");
    }

    @GetMapping("/hello2")
    public ResponseEntity<String> sayHello(Principal principal) {
        // Principal chứa thông tin người dùng đã được xác thực từ token
        return ResponseEntity.ok("Xin chào, " + principal.getName() + "! Bạn đã truy cập thành công API được bảo vệ.");
    }

}
