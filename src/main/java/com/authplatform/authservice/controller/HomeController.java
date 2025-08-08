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
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService projectService;

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from AuthService Backend! (Public API)");
    }

    @GetMapping("/projects/resolve")
    public ResponseEntity<ProjectIdResponse> resolveApiKey(@RequestParam("apiKey") String apiKey) {
        Long projectId = projectService.getProjectIdByApiKey(apiKey);
        return ResponseEntity.ok(new ProjectIdResponse(projectId));
    }
}
