package com.example.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirewallService {

    private final UdsClient udsClient;

    public Map<String, Object> getL3FilteringRule(int page) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", new HashMap<String, String>());

        String[] parsedResult = udsClient.sendMessage(UdsCmd.ShowL3FilteringRule,
            String.valueOf(page));

        if (parsedResult.length > 2 && "0".equals(parsedResult[1]) && !"-".equals(
            parsedResult[2])) {
            List<Map<String, String>> rulesList = new ArrayList<>();
            parsedResult = Arrays.copyOfRange(parsedResult, 2, parsedResult.length);
            int count = parsedResult.length / 8;

            for (int n = 0; n < count; n++) {
                Map<String, String> rule = new HashMap<>();
                rule.put("dir", parsedResult[n * 8]);
                rule.put("interface", parsedResult[n * 8 + 1]);
                rule.put("policy", parsedResult[n * 8 + 2]);
                rule.put("proto", parsedResult[n * 8 + 3]);
                rule.put("sip", parsedResult[n * 8 + 4]);
                rule.put("sport", parsedResult[n * 8 + 5].replace(",", ":"));
                rule.put("dip", parsedResult[n * 8 + 6]);
                rule.put("dport", parsedResult[n * 8 + 7].replace(",", ":"));
                rulesList.add(rule);
            }

            result.put("rulesList", rulesList);
        } else {
            result.put("success", "1");
            result.put("log", "x Failed to get filter rules list");
        }

        return result;
    }

    public Map<String, Object> setL3FilteringRule(
        String set,
        String dir,
        String iface,
        String policy,
        String proto,
        String sip,
        String sport,
        String dip,
        String dport
    ) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("act", "1");

        Map<String, String> logMap = new HashMap<>();

        //TODO need to add validation check

        log.info("{}{} {} {} {} {} {} {} {} ",
            set.equals("1") ? UdsCmd.CreateL3FilteringRule.getCmd() :
                UdsCmd.DeleteL3FilteringRule.getCmd(), dir, iface, policy, proto, sip, sport, dip,
            dport);

        String[] parsedResult = udsClient.sendMessage(
            set.equals("1") ? UdsCmd.CreateL3FilteringRule : UdsCmd.DeleteL3FilteringRule,
            new StringBuilder()
                .append(dir).append(" ")
                .append(iface).append(" ")
                .append(policy).append(" ")
                .append(proto).append(" ")
                .append(sip).append(" ")
                .append(sport).append(" ")
                .append(dip).append(" ")
                .append(dport).append(" ")
                .toString()
        );

        try {
            if (parsedResult.length > 1 && "0".equals(parsedResult[1])) {
                if (set.equals("1")) {
                    logMap.put("eng", "v Succeeded to add filter rule");
                    logMap.put("kor", "v 필터 규칙 추가에 성공했습니다");
                } else {
                    logMap.put("eng", "v Succeeded to delete filter rule");
                    logMap.put("kor", "v 필터 규칙 삭제에 성공했습니다");
                }
            } else {
                result.put("success", "1");
                if (set.equals("1")) {
                    logMap.put("eng", "x Failed to add filter rule");
                    logMap.put("kor", "x 필터 규칙 추가에 실패했습니다");
                } else {
                    logMap.put("eng", "x Failed to delete filter rule");
                    logMap.put("kor", "x 필터 규칙 삭제에 실패했습니다");
                }
            }
        } catch (Exception e) {
            result.put("success", "1");
            if (set.equals("1")) {
                logMap.put("eng", "x Failed to add filter rule");
                logMap.put("kor", "x 필터 규칙 추가에 실패했습니다");
            } else {
                logMap.put("eng", "x Failed to delete filter rule");
                logMap.put("kor", "x 필터 규칙 삭제에 실패했습니다");
            }
        }

        result.put("log", logMap);
        return result;
    }
}
