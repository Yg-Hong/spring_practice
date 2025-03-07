package com.example.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortMappingService {

    private final UdsClient udsClient;

    public Map<String, Object> getPortMappingRule(int page) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", new HashMap<String, String>());

        String[] parsedResult = udsClient.sendMessage(UdsCmd.ShowPortMappingRule,
            String.valueOf(page));

        if (parsedResult.length > 2 && "0".equals(parsedResult[1]) && !"-".equals(
            parsedResult[2])) {
            parsedResult = Arrays.copyOfRange(parsedResult, 2, parsedResult.length);
            int count = parsedResult.length / 8;

            for (int n = 0; n < count; n++) {
                Map<String, String> rule = new HashMap<>();
                rule.put("inf", parsedResult[n * 5]);
                rule.put("ip", parsedResult[n * 5 + 1]);
                rule.put("proto", parsedResult[n * 5 + 2]);
                rule.put("eport", parsedResult[n * 5 + 3]);
                rule.put("iport", parsedResult[n * 5 + 4]);

                result.put(String.valueOf(n), rule);
            }
        } else {
            result.put("success", "1");
            result.put("log", "x Failed to get filter rules list");
        }

        return result;
    }

    public Map<String, Object> setPortMappingRule(
        String set, String ip, String iport, String eport, String inf, String proto
    ) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", new HashMap<String, String>());
        result.put("act", "1");

        Map<String, String> log = new HashMap<>();

        iport = iport.isEmpty() ? "0" : iport;
        eport = eport.isEmpty() ? "0" : eport;

        //TODO add validation check

        String[] parsedResult = udsClient.sendMessage(
            set.equals("1") ? UdsCmd.CreatePortMappingRule : UdsCmd.DeletePortMappingRule,
            inf + " "
                + ip + " "
                + proto + " "
                + eport + " "
                + iport
        );

        if (parsedResult.length > 1 && "0".equals(parsedResult[1])) {
            if (set.equals("1")) {
                log.put("eng", "v Succeeded to add port mapping rule");
                log.put("kor", "v 포트 맵핑 규칙 추가에 성공했습니다");
            } else {
                log.put("eng", "v Succeeded to delete port mapping rule");
                log.put("kor", "v 포트 맵핑 규칙 삭제에 성공했습니다");
            }
        } else {
            result.put("success", "1");
            if (set.equals("1")) {
                log.put("eng", "x Failed to add port mapping rule");
                log.put("kor", "x 포트 맵핑 규칙 추가에 실패했습니다");
            } else {
                log.put("eng", "x Failed to delete port mapping rule");
                log.put("kor", "x 포트 맵핑 규칙 삭제에 실패했습니다");
            }
        }

        result.put("log", log);
        return result;
    }

}
