package com.example.project.firebase;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FirebaseService {

    private static final String API_KEY = "AIzaSyCWmdB625x7zcM8Fjc07817rMGhx3QQUes";
    private static final String DATABASE_URL =
            "https://hospital-management-syst-dc582-default-rtdb.asia-southeast1.firebasedatabase.app/";


    private static final HttpClient client = HttpClient.newHttpClient();


    public static String registerUser(String email, String password) throws Exception {

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;

        String json = """
                {
                  "email": "%s",
                  "password": "%s",
                  "returnSecureToken": true
                }
                """.formatted(email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    // ===============================
    // 🔐 LOGIN USER
    // ===============================
    public static String loginUser(String email, String password) throws Exception {

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

        String json = """
                {
                  "email": "%s",
                  "password": "%s",
                  "returnSecureToken": true
                }
                """.formatted(email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    // ===============================
    // 💾 SAVE USER PROFILE
    // ===============================
    public static void saveUserProfile(String uid,
                                       String email,
                                       String username,
                                       String role) throws Exception {

        String url = DATABASE_URL + "users/" + uid + ".json";

        String json = """
                {
                  "email": "%s",
                  "username": "%s",
                  "role": "%s"
                }
                """.formatted(email, username, role);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    // ===============================
    // 🔎 GET EMAIL BY USERNAME
    // ===============================
    public static String getEmailByUsername(String username) throws Exception {

        String url = DATABASE_URL + "users.json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        if (root == null || root.isNull()) {
            return null;
        }

        for (JsonNode user : root) {
            if (user.has("username") &&
                    user.get("username").asText().equals(username)) {

                return user.get("email").asText();
            }
        }

        return null;
    }


    // ===============================
    // 🎭 GET USER ROLE
    // ===============================
    public static String getUserRole(String uid) throws Exception {

        String url = DATABASE_URL + "users/" + uid + ".json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();

        if (body != null && body.contains("\"role\":\"")) {
            return body.split("\"role\":\"")[1].split("\"")[0];
        }

        return null;
    }
}
