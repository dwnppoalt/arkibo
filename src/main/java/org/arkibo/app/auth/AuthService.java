package org.arkibo.app.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.arkibo.dto.UserCreateRequest;
import org.arkibo.models.User.GoogleUserInfo;
import org.arkibo.models.User.User;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;

import com.google.gson.JsonParser;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;

import org.apache.http.client.utils.URIBuilder;
import com.sun.net.httpserver.HttpServer;

import io.github.cdimascio.dotenv.Dotenv;
import java.awt.Desktop;
import java.util.List;
import java.util.UUID;



public class AuthService {

    private static class CallbackServer {
        public final HttpServer server;
        public final CompletableFuture<String> future;
        public final int port;

        public CallbackServer(HttpServer server, CompletableFuture<String> future, int port) {
            this.server = server;
            this.future = future;
            this.port = port;
        }
    }

    Dotenv dotenv = Dotenv.load();
    private final String CLIENT_ID = dotenv.get("GOOGLE_OAUTH_CLIENT_ID");
    private final String CLIENT_SECRET = dotenv.get("GOOGLE_OAUTH_CLIENT_SECRET");
    private CallbackServer currentCallback;

    private ThesisRepository thesisRepository;
    private UserRepository userRepository;

    public AuthService(ThesisRepository thesisRepository, UserRepository userRepository) {
        this.thesisRepository = thesisRepository;
        this.userRepository = userRepository;
    }
    
    public User login() throws Exception {
        String verifier = PKCEUtil.generateCodeVerifier();
        String challenge = PKCEUtil.generateCodeChallenge(verifier);
        String state = java.util.UUID.randomUUID().toString();

        if (currentCallback != null) {
            currentCallback.server.stop(0);
            currentCallback.future.cancel(true);
            currentCallback = null;
        }

        CallbackServer callback = startCallbackServer(state);
        currentCallback = callback;

        String redirectUri = "http://127.0.0.1:" + callback.port + "/callback";

        URIBuilder builder = new URIBuilder("https://accounts.google.com/o/oauth2/v2/auth");
        builder.addParameter("client_id", CLIENT_ID);
        builder.addParameter("response_type", "code");
        builder.addParameter("scope", "openid email profile");
        builder.addParameter("redirect_uri", redirectUri);
        builder.addParameter("code_challenge", challenge);
        builder.addParameter("code_challenge_method", "S256");
        builder.addParameter("state", state);

        Desktop.getDesktop().browse(new URI(builder.build().toString()));

        try {
            String authCode = callback.future.get();

            String idToken = exchangeCodeForIdToken(authCode, verifier, redirectUri);
            GoogleUserInfo info = verifyIdToken(idToken);

            userRepository.addUser(new UserCreateRequest(
                    info.getGoogleId(),
                    info.getName(),
                    info.getEmail(),
                    info.getPictureUrl()));

            var thesisResult = thesisRepository.getUserSavedThesis(info.getGoogleId());
            List<Thesis> savedTheses = (thesisResult == null || thesisResult.data() == null)
                    ? new ArrayList<>()
                    : thesisResult.data();

            return new User(
                    info.getGoogleId(),
                    info.getName(),
                    info.getEmail(),
                    info.getPictureUrl(),
                    savedTheses);

        } finally {
            if (currentCallback != null) {
                currentCallback.server.stop(0);
                currentCallback.future.cancel(true);
                currentCallback = null;
            }
        }
    }

    private CallbackServer startCallbackServer(String expectedState) throws IOException {
        
        CompletableFuture<String> future = new CompletableFuture<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();

            String code = extractCode(query);
            String error = extractError(query);
            String returnedState = extractState(query);

            String response;

            if (expectedState != null && !expectedState.equals(returnedState)) {
                response = buildHtmlResponse(
                        "Invalid State",
                        "Security check failed. Please try again.",
                        false);
                future.completeExceptionally(new RuntimeException("Invalid state"));
            } else if (error != null) {
                response = buildHtmlResponse(
                        "Login Failed",
                        "Google returned an error: " + error,
                        false);
                future.completeExceptionally(new RuntimeException("OAuth error: " + error));
            } else if (code == null) {
                response = buildHtmlResponse(
                        "Login Failed",
                        "No authorization code received.",
                        false);
                future.completeExceptionally(new RuntimeException("No auth code received"));
            } else {
                response = buildHtmlResponse(
                        "Login Successful",
                        "You can now return to the app.",
                        true);
                future.complete(code);
            }

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.close();

            server.stop(0);
        });

        server.start();

        return new CallbackServer(server, future, port);
    }

    private String exchangeCodeForIdToken(String code, String verifier, String redirectUri) throws Exception {

        URIBuilder builder = new URIBuilder("https://oauth2.googleapis.com/token");
        builder.addParameter("client_id", CLIENT_ID);
        builder.addParameter("client_secret", CLIENT_SECRET);
        builder.addParameter("code", code);
        builder.addParameter("code_verifier", verifier);
        builder.addParameter("redirect_uri", redirectUri);
        builder.addParameter("grant_type", "authorization_code");

        String body = builder.build().getQuery();

        URL url = new URI("https://oauth2.googleapis.com/token").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
        conn.getOutputStream().write(bodyBytes);

        int status = conn.getResponseCode();
        java.io.InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            throw new IOException("No response from token endpoint, status=" + status);
        }

        String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        if (!json.has("id_token")) {
            throw new RuntimeException("Token response missing id_token: " + response);
        }

        return json.get("id_token").getAsString();
    }

    private GoogleUserInfo verifyIdToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()).setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null)
            throw new RuntimeException("Invalid ID token");

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleId = payload.getSubject();
        String pictureUrl = (String) payload.get("picture");

        return new GoogleUserInfo(email, name, googleId, pictureUrl);
    }

    private String extractCode(String query) {
        if (query == null || query.isEmpty())
            return null;

        for (String param : query.split("&")) {
            if (param.startsWith("code=")) {
                String value = param.substring(5);
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private String extractError(String query) {
        if (query == null || query.isEmpty())
            return null;

        for (String param : query.split("&")) {
            if (param.startsWith("error=")) {
                return URLDecoder.decode(param.substring(6), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String extractState(String query) {
        if (query == null || query.isEmpty())
            return null;

        for (String param : query.split("&")) {
            if (param.startsWith("state=")) {
                return URLDecoder.decode(param.substring(6), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String buildHtmlResponse(String title, String message, boolean success) {
        String color = success ? "#22c55e" : "#ef4444";

        return """
                <html>
                <head>
                    <title>%s</title>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="
                    margin: 0;
                    height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background-color: #0f172a;
                    color: #e5e7eb;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    text-align: center;
                ">
                    <div>
                        <h2 style="margin: 0 0 10px 0; color: %s;">%s</h2>
                        <p style="margin: 0; font-size: 14px; color: #cbd5f5;">%s</p>
                    </div>
                </body>
                </html>
                """.formatted(title, color, title, message);
    }
}
