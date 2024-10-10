package example.fetcher.collaborator;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.Interaction;

import example.fetcher.collaborator.poller.InteractionHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MyInteractionHandler implements InteractionHandler {
    private final MontoyaApi api;
    private int id = 0;

    public MyInteractionHandler(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public void handleInteraction(Interaction interaction) {
        id += 1;
        byte[] requestBytes;

        if (interaction.type().name().equalsIgnoreCase("http")) {
            String requestString = interaction.httpDetails().get().requestResponse().request().toString();
            requestBytes = requestString.getBytes(StandardCharsets.UTF_8);
        } else if (interaction.type().name().equalsIgnoreCase("dns")) {
            requestBytes = interaction.dnsDetails().get().query().getBytes();
        } else {
            return;
        }

        String base64EncodedRequest = Base64.getEncoder().encodeToString(requestBytes);

        Map<String, String> data = new HashMap<>();
        data.put(id + "-" + interaction.id().toString() + "-" + interaction.type().name().toLowerCase(), base64EncodedRequest);

        StringBuilder jsonBodyBuilder = new StringBuilder();
        jsonBodyBuilder.append("{");

        for (Map.Entry<String, String> entry : data.entrySet()) {
            jsonBodyBuilder.append("\"").append(entry.getKey()).append("\": ")
                    .append("\"").append(entry.getValue()).append("\",");
        }

        jsonBodyBuilder.setLength(jsonBodyBuilder.length() - 1);
        jsonBodyBuilder.append("}");

        String jsonBody = jsonBodyBuilder.toString();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8888/add-interaction"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ignored) {}
    }
}
