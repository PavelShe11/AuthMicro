package io.github.pavelshe11.authmicro.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    @PostMapping("/user")
    public String userAuthorisation() {
        return "This is user!";
    }


    @PostMapping("/public")
    public String withoutAuthorisation() {
        return "This is public!";
    }


    @PostMapping("/admin")
    public String adminAuthorisation() {
        return "This is admin!";
    }
}
