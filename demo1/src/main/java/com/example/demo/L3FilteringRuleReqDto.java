package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class L3FilteringRuleReqDto {

    String set;
    String dir;
    String iface;
    String policy;
    String proto;
    String sip;
    String sport;
    String dip;
    String dport;
}
