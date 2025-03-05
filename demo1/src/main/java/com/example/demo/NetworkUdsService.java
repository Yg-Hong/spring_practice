package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkUdsService {

    private final UdsClient udsClient;

    public Map<String, Object> getEditedRList(int page) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");

        String[] parsedResult = udsClient.sendMessage(UdsCmd.EditedRoutingList,
            String.valueOf(page));

        if (parsedResult.length > 2 && "0".equals(parsedResult[1]) && !"-".equals(
            parsedResult[2])) {
            int count = (parsedResult.length - 2) / 3;

            for (int n = 0; n < count; n++) {
                Map<String, String> routeEntry = new HashMap<>();
                routeEntry.put("dip", parsedResult[2 + (n * 3)]);
                routeEntry.put("nmask", parsedResult[3 + (n * 3)]);
                routeEntry.put("gw", parsedResult[4 + (n * 3)]);
                result.put(String.valueOf(n), routeEntry);
            }
        } else if (parsedResult.length > 2 && "0".equals(parsedResult[1])) {
            result.put("success", "0");
        } else {
            result.put("success", "1");
        }

        return result;
    }

    public Map<String, Object> getTotalRList() throws IOException {
        Map<String, Object> result = new HashMap<>();
        int count = 0;

        String msg = executeCommand("route -n");
        if (msg.isEmpty()) {
            result.put("success", "0");
            result.put("log", "Failed to fetch routing table");
            return result;
        }

        String[] lines = msg.split("\n");

        // 첫 번째 줄(헤더) 제외
        for (String s : lines) {
            String line = s.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            List<String> row = new ArrayList<>();
            for (String token : tokens) {
                row.add(token);
            }
            result.put(String.valueOf(count), row);

            count++;
        }

        result.put("success", "0");
        result.put("rcount", count);

        return result;
    }

    public Map<String, Object> createRoutingRule(String dip, String subnetMask, String gateway)
        throws IOException {
        //TODO data validation check
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", new HashMap<String, String>());
        result.put("act", "1");

        Map<String, String> log = new HashMap<>();

        StringBuilder sb = new StringBuilder()
            .append("ip route ")
            .append(dip).append(" ")
            .append(subnetMask).append(" ")
            .append(gateway);
        String[] parsedResult = udsClient.sendMessage(UdsCmd.AddRoutingRule, sb.toString());

        if (parsedResult.length > 1 && "0".equals(parsedResult[1])) {
            log.put("eng", "v Succeeded to add routing rule");
            log.put("kor", "v 라우팅 규칙 추가에 성공했습니다");
        } else {
            result.put("success", "1");
            log.put("eng", "x Failed to add routing rule");
            log.put("kor", "x 라우팅 규칙 추가에 실패했습니다");
        }

        result.put("log", log);
        return result;
    }

    public Map<String, Object> removeRoutingRule(String dip, String subnetMask, String gateway)
        throws IOException {
        //TODO data validation check
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", new HashMap<String, String>());
        result.put("act", "1");

        Map<String, String> log = new HashMap<>();

        StringBuilder sb = new StringBuilder()
            .append("no ip route ")
            .append(dip).append(" ")
            .append(subnetMask);
        String[] parsedResult = udsClient.sendMessage(UdsCmd.DeleteRoutingRule, sb.toString());

        if (parsedResult.length > 1 && "0".equals(parsedResult[1])) {
            log.put("eng", "v Succeeded to delete routing rule");
            log.put("kor", "v 라우팅 규칙 삭제에 성공했습니다");
        } else {
            result.put("success", "1");
            log.put("eng", "x Failed to delete routing rule");
            log.put("kor", "x 라우팅 규칙 삭제에 실패했습니다");
        }

        result.put("log", log);
        return result;
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            return "";
        }
        return output.toString();
    }

}
