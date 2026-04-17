package org.arkibo.utils;

import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LocalPdfServer extends NanoHTTPD {

    private final HttpClient httpClient;

    public LocalPdfServer(int port) {
        super(port);
        this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/")) {
            uri = "/viewer.html";
        }

        if (uri.contains("..")) {
            return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "403");
        }

        if (uri.equals("/proxy-pdf")) {
            return serveProxiedPdf(session);
        }

        try {
            InputStream is = getClass().getResourceAsStream("/reader" + uri);

            if (is == null) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404");
            }

            String mime = getMimeForUri(uri);
            Response response = newChunkedResponse(Response.Status.OK, mime, is);
            response.addHeader("Cache-Control", "no-cache");
            return response;

        } catch (Exception e) {
            return newFixedLengthResponse("Error: " + e.getMessage());
        }
    }

    private Response serveProxiedPdf(IHTTPSession session) {
        try {
            String encodedUrl = session.getParms().get("url");
            if (encodedUrl == null || encodedUrl.isBlank()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing url");
            }

            String sourceUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl)).GET().build();

            HttpResponse<byte[]> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (upstream.statusCode() < 200 || upstream.statusCode() >= 300) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "text/plain",
                        "Upstream status: " + upstream.statusCode());
            }

            String contentType = upstream.headers().firstValue("Content-Type").orElse("application/pdf");
            byte[] body = upstream.body();

            Response response = newFixedLengthResponse(
                    Response.Status.OK,
                    contentType,
                    new ByteArrayInputStream(body),
                    body.length);
            response.addHeader("Cache-Control", "no-cache");
            return response;
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Proxy error: " + e.getMessage());
        }
    }

    private String getMimeForUri(String uri) {
        String lower = uri.toLowerCase();

        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "text/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".wasm")) return "application/wasm";
        if (lower.endsWith(".bcmap")) return "application/octet-stream";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ttf")) return "font/ttf";
        if (lower.endsWith(".otf")) return "font/otf";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".woff2")) return "font/woff2";

        return getMimeTypeForFile(uri);
    }
}