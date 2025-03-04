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
    ;

    private final String cmd;
}
