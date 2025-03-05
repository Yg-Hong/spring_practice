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
public class PufUdsService {

    private final UdsClient udsClient;

    public Map<String, Object> getMasterPufStatus() throws IOException {
        String[] parsedResult = udsClient.sendMessage(UdsCmd.showPufMaster);
        return getPufStatus(parsedResult);
    }

    public Map<String, Object> getSlavePufStatus() throws IOException {
        String[] parsedResult = udsClient.sendMessage(UdsCmd.showPufSlave);
        return getPufStatus(parsedResult);
    }

    public Map<String, String> getPciInfo() throws IOException {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");

        List<String> output = executeCommand("pv_pufd --pci_info 2>&1");

        if (output.isEmpty()) {
            result.put("success", "1");
            result.put("log", "x Failed to get PCI info");
        } else {
            for (int i = 0; i < output.size(); i++) {
                result.put(String.valueOf(i), output.get(i));
            }
            result.put("lcount", String.valueOf(output.size()));
        }

        return result;
    }

    private Map<String, Object> getPufStatus(String[] parsedResult) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");
        int err = 0;

        if (parsedResult.length > 3 && "0".equals(parsedResult[1])) {
            result.put("slot", parsedResult[2]);
            result.put("all_g3", String.valueOf(Integer.parseInt(parsedResult[3]) / 2));

            List<Map<String, String>> errG3Num = new ArrayList<>();
            int count = parsedResult.length - 5;

            for (int n = 0; n < count; n++) {
                Map<String, String> g3 = new HashMap<>();
                String[] config = parsedResult[5 + n].split(",");
                if (config.length >= 4) {
                    g3.put("role", config[0]);
                    g3.put("port", config[1]);
                    g3.put("group", config[2]);
                    g3.put("num", config[3]);
                    errG3Num.add(g3);
                    err++;
                }
            }

            result.put("err_g3_num", errG3Num);
            result.put("err_g3", String.valueOf(err));
        } else {
            result.put("success", "1");
            result.put("log", "x Failed to get puf array status");
        }

        return result;
    }

    private List<String> executeCommand(String command) {
        List<String> output = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output.add("Error executing command: " + e.getMessage());
        }
        return output;
    }

}
