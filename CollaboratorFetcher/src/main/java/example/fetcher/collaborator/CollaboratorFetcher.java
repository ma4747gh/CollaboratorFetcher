package example.fetcher.collaborator;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.collaborator.SecretKey;

import example.fetcher.collaborator.poller.Poller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class CollaboratorFetcher implements BurpExtension {
    private MontoyaApi api;
    private static Process webApplicationInterfaceProcess;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        api.extension().setName("Collaborator Fetcher");

        api.logging().logToOutput("Opening Web Interface...");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-jar", "WebApplicationInterface-1.0.jar"
            );
            webApplicationInterfaceProcess = processBuilder.start();

            sleep(4000);
        } catch (IOException | InterruptedException ignored) {}

        CollaboratorClient collaboratorClient = createCollaboratorClient(api.persistence().extensionData());

        Poller collaboratorPoller = new Poller(collaboratorClient, Duration.ofSeconds(10));
        collaboratorPoller.registerInteractionHandler(new MyInteractionHandler(api));

        api.extension().registerUnloadingHandler(() -> {
            if (webApplicationInterfaceProcess != null && webApplicationInterfaceProcess.isAlive()) {
                webApplicationInterfaceProcess.destroy();
            }
            collaboratorPoller.shutdown();
            api.logging().logToOutput("Extension unloading...");
        });

        String numberOfPayloads = getNumberOfPayloads();

        generatePayloads(collaboratorClient, Integer.parseInt(numberOfPayloads));

        collaboratorPoller.start();
    }

    private CollaboratorClient createCollaboratorClient(PersistedObject persistedData) {
        CollaboratorClient collaboratorClient;

        String existingCollaboratorKey = persistedData.getString("persisted_collaborator");

        if (existingCollaboratorKey != null) {
            api.logging().logToOutput("Creating Collaborator client from key.");
            collaboratorClient = api.collaborator().restoreClient(SecretKey.secretKey(existingCollaboratorKey));
        }
        else {
            api.logging().logToOutput("No previously found Collaborator client. Creating new client...");
            collaboratorClient = api.collaborator().createClient();

            api.logging().logToOutput("Saving Collaborator secret key.");
            persistedData.setString("persisted_collaborator", collaboratorClient.getSecretKey().toString());
        }

        return collaboratorClient;
    }

    private String getNumberOfPayloads() {
        HttpClient client = HttpClient.newHttpClient();
        String numberOfPayloads;

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8888/start"))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String responseBody = response.body();

                Map<String, String> responseBodyMap;
                responseBodyMap = parseJson(responseBody);

                if (!responseBodyMap.isEmpty()) {
                    numberOfPayloads = responseBodyMap.get("num-payloads");
                    break;
                }

                Thread.sleep(4000);
            } catch (IOException | InterruptedException ignored) {}
        }

        return numberOfPayloads;
    }

    private void generatePayloads(CollaboratorClient collaboratorClient, int totalPayloads) {
        Map<String, String> payloads = new HashMap<>();

        for (int i = 1; i <= totalPayloads; i++) {
            String payload = collaboratorClient.generatePayload().toString();
            payloads.put(String.valueOf(i), payload);
        }

        StringBuilder jsonBodyBuilder = new StringBuilder();
        jsonBodyBuilder.append("{");

        for (Map.Entry<String, String> entry : payloads.entrySet()) {
            jsonBodyBuilder.append("\"").append(entry.getKey()).append("\": ")
                    .append("\"").append(entry.getValue()).append("\",");
        }

        jsonBodyBuilder.setLength(jsonBodyBuilder.length() - 1);
        jsonBodyBuilder.append("}");

        String jsonBody = jsonBodyBuilder.toString();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8888/add-payload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ignored) {}
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();

        json = json.trim();
        if (json.equals("{}")) {
            return map;
        }

        json = json.substring(1, json.length() - 1).trim();

        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }
        return map;
    }
}
