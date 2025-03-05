package com.example.demo;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uds")
@RequiredArgsConstructor
public class UdsController {

    private final UdsClient udsClientService;
    private final OverviewUdsService overviewUdsService;
    private final NetworkUdsService networkUdsService;

    @PostMapping("/ping")
    public String[] ping() throws IOException {
        return udsClientService.sendMessage(UdsCmd.Ping);
    }

    @GetMapping("/showDaemon")
    public Map<String, Object> showDaemon() throws IOException {
        return overviewUdsService.getOverviewDaemonStatus();
    }

    @GetMapping("/homeinfo")
    public Map<String, Object> homeinfo() throws IOException {
        return overviewUdsService.getOverviewHomeInfo();
    }

    @GetMapping("/edited-rlist")
    public Map<String, Object> getEditedRList(@RequestParam int page) throws IOException {
        return networkUdsService.getEditedRList(page);
    }

    @GetMapping("/rlist")
    public Map<String, Object> getTotalRList() throws IOException {
        return networkUdsService.getTotalRList();
    }

    @PostMapping("rlist")
    public Map<String, Object> addRoutingRule(@RequestBody RoutingRuleReqDto routingRuleReqDto)
        throws IOException {
        return networkUdsService.createRoutingRule(
            routingRuleReqDto.getDestinationIp(),
            routingRuleReqDto.getSubnetMask(),
            routingRuleReqDto.getGateway()
        );
    }
}

