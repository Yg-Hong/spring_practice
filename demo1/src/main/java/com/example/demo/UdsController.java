package com.example.demo;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    private final UdsClient udsClientService;

    @PostMapping("/send")
    public String[] sendPacket(@RequestParam String message) throws IOException {
        return udsClientService.sendMessage(message);
    }

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage("12:0:0\n");
    }

    @PostMapping("/showDaemon")
    public String[] showDaemon() throws IOException {
        return udsClientService.sendMessage("161:0:0\n");
    }
}
