package com.example.demo;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final PufUdsService pufUdsService;
    private final VpnUdsService vpnUdsService;

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

    @PostMapping("/rrule")
    public Map<String, Object> addRoutingRule(@RequestBody RoutingRuleReqDto routingRuleReqDto)
        throws IOException {
        return networkUdsService.createRoutingRule(
            routingRuleReqDto.getDestinationIp(),
            routingRuleReqDto.getSubnetMask(),
            routingRuleReqDto.getGateway()
        );
    }

    @DeleteMapping("/rrule")
    public Map<String, Object> deleteRoutingRule(@RequestParam String destinationIp,
        @RequestParam String subnetMask)
        throws IOException {
        return networkUdsService.removeRoutingRule(
            destinationIp,
            subnetMask
        );
    }

    @GetMapping("/master")
    public Map<String, Object> getMasterPUFStatus() throws IOException {
        return pufUdsService.getMasterPufStatus();
    }

    @GetMapping("/slave")
    public Map<String, Object> getSlavePUFStatus() throws IOException {
        return pufUdsService.getSlavePufStatus();
    }

    @GetMapping("/pci")
    public Map<String, String> getPciPUFStatus() throws IOException {
        return pufUdsService.getPciInfo();
    }

    @GetMapping("/clients")
    public Map<String, Object> getClientsByPage(@RequestParam int page, @RequestParam int rowNum)
        throws IOException {
        return vpnUdsService.getClientsByPage(page, rowNum);
    }
}

