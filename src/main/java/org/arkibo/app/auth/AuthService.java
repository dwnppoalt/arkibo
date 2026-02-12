package org.arkibo.app.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.arkibo.models.User.GoogleUserInfo;
import org.arkibo.models.User.User;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.utils.PKCEUtil;

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

public class AuthService {
    Dotenv dotenv = Dotenv.load();
    private final String CLIENT_ID = dotenv.get("GOOGLE_OAUTH_CLIENT_ID");
    private final String CLIENT_SECRET = dotenv.get("GOOGLE_OAUTH_CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://localhost:51743/callback";

    private ThesisRepository thesisRepository = new ThesisRepository();

    public User login() throws Exception {
        String verifier = PKCEUtil.generateCodeVerifier();
        String challenge = PKCEUtil.generateCodeChallenge(verifier);

        URIBuilder builder = new URIBuilder("https://accounts.google.com/o/oauth2/v2/auth");
        builder.addParameter("client_id", CLIENT_ID);
        builder.addParameter("response_type", "code");
        builder.addParameter("scope", "openid email profile");
        builder.addParameter("redirect_uri", REDIRECT_URI);
        builder.addParameter("code_challenge", challenge);
        builder.addParameter("code_challenge_method", "S256");

        CompletableFuture<String> codeFuture = startCallbackServer();

        Desktop.getDesktop().browse(new URI(builder.build().toString()));

        String authCode = codeFuture.get();

        String idToken = exchangeCodeForIdToken(authCode, verifier);

        GoogleUserInfo info = verifyIdToken(idToken);

        return new User(
                info.getGoogleId(),
                info.getName(),
                info.getEmail(),
                info.getPictureUrl(),
                thesisRepository.getUserSavedThesis(info.getGoogleId()).data());
    }

    private CompletableFuture<String> startCallbackServer() throws IOException {
        CompletableFuture<String> future = new CompletableFuture<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(51743), 0);

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = extractCode(query);
            String response = "Login successful. You can close this tab.";

            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody()
                    .write(response.getBytes());
            exchange.close();

            future.complete(code);
            server.stop(0);
        });

        server.start();
        return future;
    }

    private String exchangeCodeForIdToken(String code, String verifier) throws Exception {

        URIBuilder builder = new URIBuilder("https://oauth2.googleapis.com/token");
        builder.addParameter("client_id", CLIENT_ID);
        builder.addParameter("client_secret", CLIENT_SECRET);
        builder.addParameter("code", code);
        builder.addParameter("code_verifier", verifier);
        builder.addParameter("redirect_uri", REDIRECT_URI);
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

}
