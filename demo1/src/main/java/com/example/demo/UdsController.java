package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    //    private final UdsClient udsClientService;
    private final UdsDatagramClient udsClientService;

    @PostMapping("/send")
    public String sendPacke(@RequestParam String message) {
        return udsClientService.sendPacket(message, 5);
    }

    @PostMapping("/ping")
    public String ping() {
        return udsClientService.sendPacket("12:0:0", 5);
    }

}
