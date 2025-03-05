package com.example.demo;

import java.io.IOException;
import java.util.HashMap;
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
        result.put("success", "0");
        result.put("log", "");

        String[] parsedResult = udsClient.sendMessage(UdsCmd.TotalRoutingList);

        String msg = executeCommand("route -n");
        log.info(msg);

        return null;
    }

    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

}
