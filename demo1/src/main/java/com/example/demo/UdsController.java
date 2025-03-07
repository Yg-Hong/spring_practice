package com.example.demo;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    private final UdsClient udsClientService;
    private final VpnUdsService vpnUdsService;

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage(UdsCmd.Ping);
    }

    @GetMapping("/clients")
    public Map<String, Object> showClientsByPage(@RequestParam int page, @RequestParam int rowNum)
        throws IOException {
        return vpnUdsService.getClientsByPage(page, rowNum);
    }

    @GetMapping("/tunnels")
    public Map<String, Object> showTunnels() throws IOException {
        return vpnUdsService.getTunnels();
    }
}

