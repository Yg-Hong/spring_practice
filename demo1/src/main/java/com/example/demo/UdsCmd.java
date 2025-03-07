package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UdsCmd {

    // Test
    Ping("12:0:0"),

    // Overview Status
    OverviewDaemonStatus("161:0:0"),
    OverviewHomeInfo("21:0:0"),

    // Routing
    EditedRoutingList("36:0:"),
    TotalRoutingList("37:0:0"),
    AddRoutingRule("11:0:"),
    DeleteRoutingRule("11:0:"),

    // PUF
    showPufMaster("171:0:0"),
    showPufSlave("172:0:0"),

    // VPN

    // Firewall
    ShowL3FilteringRule("51:0:"),
    CreateL3FilteringRule("11:0:zfirewall filter "),
    DeleteL3FilteringRule("11:0:no zfirewall filter "),

    // Port Mapping
    ShowPortMappingRule("52:0:"),
    CreatePortMappingRule("11:0:zfirewall nat portmap "),
    DeletePortMappingRule("11:0:no zfirewall nat portmap "),
    ;

    private final String cmd;
}
