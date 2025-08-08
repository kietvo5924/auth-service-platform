package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ProjectIdResponse;
import com.authplatform.authservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService projectService;

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from AuthService Backend! (Public API)");
    }

    @GetMapping("/hello2")
    public ResponseEntity<String> sayHello(Principal principal) {
        // Principal chứa thông tin người dùng đã được xác thực từ token
        return ResponseEntity.ok("Xin chào, " + principal.getName() + "! Bạn đã truy cập thành công API được bảo vệ.");
    }

    @GetMapping("/projects/resolve")
    public ResponseEntity<ProjectIdResponse> resolveApiKey(@RequestParam("apiKey") String apiKey) {
        Long projectId = projectService.getProjectIdByApiKey(apiKey);
        return ResponseEntity.ok(new ProjectIdResponse(projectId));
    }
}
