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
