package com.example.demo;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/firewall")
@Slf4j
public class FirewallController {

    private final FirewallService firewallUdsService;
    private final PortMappingService portMappingService;

    @GetMapping("/l3")
    public Map<String, Object> showL3FilteringRule(@RequestParam int page) throws IOException {
        return firewallUdsService.getL3FilteringRule(page);
    }

    @PostMapping("/l3")
    public Map<String, Object> setL3FilteringRule(
        @RequestBody L3FilteringRuleReqDto l3FilteringRuleReqDto) throws IOException {
        log.info(l3FilteringRuleReqDto.toString());
        return firewallUdsService.setL3FilteringRule(
            l3FilteringRuleReqDto.getSet(),
            l3FilteringRuleReqDto.getDir(),
            l3FilteringRuleReqDto.getIface(),
            l3FilteringRuleReqDto.getPolicy(),
            l3FilteringRuleReqDto.getProto(),
            l3FilteringRuleReqDto.getSip(),
            l3FilteringRuleReqDto.getSport(),
            l3FilteringRuleReqDto.getDip(),
            l3FilteringRuleReqDto.getDport()
        );
    }

    @GetMapping("/port-mapping")
    public Map<String, Object> showPortMappingRule(@RequestParam int page) throws IOException {
        return portMappingService.getPortMappingRule(page);
    }

    @PostMapping("/port-mapping")
    public Map<String, Object> setPortMappingRule(
        @RequestBody PortMappingRuleReqDto portMappingRuleReqDto) throws IOException {
        return portMappingService.setPortMappingRule(
            portMappingRuleReqDto.getSet(),
            portMappingRuleReqDto.getIp(),
            portMappingRuleReqDto.getIport(),
            portMappingRuleReqDto.getEport(),
            portMappingRuleReqDto.getInf(),
            portMappingRuleReqDto.getProto()
        );
    }
}
