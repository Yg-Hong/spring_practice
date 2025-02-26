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
//    private final UdsDatagramClient udsClientService;

    @PostMapping("/send")
    public String[] sendPacket(@RequestParam String message) throws IOException {
        return udsClientService.sendMessage(message);
    }

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage("12:0:0");
    }

    @PostMapping("/showDaemon")
    public String[] showDaemon() throws IOException {
        return udsClientService.sendMessage("161:0:0");
    }


    @PostMapping("/ping2")
    public String ping2() throws IOException {
        return udsClientService.sendPacket("12:0:0", 1);
    }

    @PostMapping("/showDaemon2")
    public String StringshowDaemon2() throws IOException {
        return udsClientService.sendPacket("161:0:0", 5);
    }
}
