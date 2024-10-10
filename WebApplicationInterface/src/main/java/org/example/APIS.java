package org.example;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class APIS {
    private Map<String, String> numPayloads = new HashMap<>();
    private Map<String, String> payloads = new HashMap<>();
    private Map<String, String> data = new HashMap<>();

    @GetMapping("/")
    public Map<String, String> mainPage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the API! Use /start to initialize payloads.");
        return response;
    }

    @GetMapping("/start")
    public Map<String, String> start(@RequestParam(value = "total-payloads", required = false) String totalPayloads) {
        if (totalPayloads != null & numPayloads.isEmpty()) {
            numPayloads.put("num-payloads", totalPayloads);
        } else if (totalPayloads != null & !numPayloads.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You cannot modify the number of payloads. Please reload the extension.");
            return response;
        }
        return numPayloads;
    }

    @PostMapping("/add-payload")
    public Map<String, String> addPayload(@RequestBody Map<String, String> entry) {
        payloads.putAll(entry);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payload added successfully!");
        response.put("added_payloads", String.valueOf(entry.size()));

        return response;
    }

    @GetMapping("/get-payloads")
    public Map<String, String> getPayloads() {
        return payloads;
    }

    @PostMapping("/add-interaction")
    public Map<String, String> addInteraction(@RequestBody Map<String, String> entry) {
        data.putAll(entry);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Interaction added successfully!");
        response.put("added_entries", String.valueOf(entry.size()));

        return response;
    }

    @GetMapping("/get-interactions")
    public Map<String, String> getInteractions() {
        return data;
    }
}
