package com.example.demo;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    private final UdsClient udsClientService;
    private final OverviewUdsService overviewUdsService;

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage(UdsCmd.Ping);
    }

    @PostMapping("/showDaemon")
    public Map<String, Object> showDaemon() throws IOException {
//        return udsClientService.sendMessage(UdsCmd.OverviewDaemonStatus);
        return overviewUdsService.getOverviewDaemonStatus();
    }
}
