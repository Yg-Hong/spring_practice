package com.example.demo;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    private final UdsClient udsClientService;

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage(UdsCmd.Ping);
    }

    @PostMapping("/showDaemon")
    public String[] showDaemon() throws IOException {
        return udsClientService.sendMessage(UdsCmd.OverviewDaemonStatus);
    }
}
