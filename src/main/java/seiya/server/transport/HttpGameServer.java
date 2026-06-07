package seiya.server.transport;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import seiya.server.api.ApiModels;
import seiya.server.api.GameApi;
import seiya.server.session.GameSessionRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public final class HttpGameServer {
    private final int port;
    private final GameApi gameApi = new GameApi(new GameSessionRegistry());
    private final JsonCodec jsonCodec = new JsonCodec();
    private HttpServer server;

    public HttpGameServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Seiya web multiplayer running at http://localhost:" + port);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                sendText(exchange, 200, "ok");
                return;
            }
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
                return;
            }
            if (path.startsWith("/assets/")) {
                serveAsset(exchange, path.substring("/assets/".length()));
                return;
            }
            serveResource(exchange, "web/index.html", "text/html; charset=utf-8");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, new ApiModels.ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            sendJson(exchange, 409, new ApiModels.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(exchange, 500, new ApiModels.ErrorResponse("Server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        String route = versionlessRoute(path);
        if ("GET".equals(exchange.getRequestMethod()) && "/api/options".equals(route)) {
            sendJson(exchange, 200, gameApi.options());
            return;
        }

        if ("POST".equals(exchange.getRequestMethod()) && "/api/rooms".equals(route)) {
            sendJson(exchange, 200, gameApi.createRoom(createRoomRequest(exchange)));
            return;
        }

        String[] parts = route.split("/");
        if (parts.length < 4 || !"api".equals(parts[1]) || !"rooms".equals(parts[2])) {
            sendJson(exchange, 404, new ApiModels.ErrorResponse("API route not found."));
            return;
        }

        String roomCode = parts[3];

        if ("POST".equals(exchange.getRequestMethod()) && parts.length == 5 && "join".equals(parts[4])) {
            sendJson(exchange, 200, gameApi.joinRoom(roomCode, joinRoomRequest(exchange)));
            return;
        }

        if ("GET".equals(exchange.getRequestMethod()) && parts.length == 4) {
            sendJson(exchange, 200, gameApi.state(roomCode, requestToken(exchange, params(exchange).get("token"))));
            return;
        }

        if ("POST".equals(exchange.getRequestMethod()) && parts.length == 5 && "actions".equals(parts[4])) {
            sendJson(exchange, 200, gameApi.submitAction(roomCode, actionRequest(exchange)));
            return;
        }

        if ("POST".equals(exchange.getRequestMethod()) && parts.length == 5 && "rematch".equals(parts[4])) {
            sendJson(exchange, 200, gameApi.rematch(roomCode, tokenRequest(exchange)));
            return;
        }

        if ("POST".equals(exchange.getRequestMethod()) && parts.length == 5 && "exit".equals(parts[4])) {
            sendJson(exchange, 200, gameApi.exit(roomCode, tokenRequest(exchange)));
            return;
        }

        sendJson(exchange, 404, new ApiModels.ErrorResponse("API route not found."));
    }

    private String versionlessRoute(String path) {
        if (path.equals("/api/v1")) {
            return "/api";
        }
        if (path.startsWith("/api/v1/")) {
            return "/api" + path.substring("/api/v1".length());
        }
        return path;
    }

    private ApiModels.CreateRoomRequest createRoomRequest(HttpExchange exchange) throws IOException {
        if (isJson(exchange)) {
            return jsonCodec.read(readAllBytes(exchange.getRequestBody()), ApiModels.CreateRoomRequest.class);
        }
        Map<String, String> params = params(exchange);
        ApiModels.CreateRoomRequest request = new ApiModels.CreateRoomRequest();
        request.setCharacter(params.get("character"));
        request.setRuleSet(params.get("ruleSet"));
        return request;
    }

    private ApiModels.JoinRoomRequest joinRoomRequest(HttpExchange exchange) throws IOException {
        if (isJson(exchange)) {
            return jsonCodec.read(readAllBytes(exchange.getRequestBody()), ApiModels.JoinRoomRequest.class);
        }
        ApiModels.JoinRoomRequest request = new ApiModels.JoinRoomRequest();
        request.setCharacter(params(exchange).get("character"));
        return request;
    }

    private ApiModels.ActionRequest actionRequest(HttpExchange exchange) throws IOException {
        ApiModels.ActionRequest request;
        if (isJson(exchange)) {
            request = jsonCodec.read(readAllBytes(exchange.getRequestBody()), ApiModels.ActionRequest.class);
        } else {
            Map<String, String> params = params(exchange);
            request = new ApiModels.ActionRequest();
            request.setToken(params.get("token"));
            request.setAction(params.get("action"));
        }
        request.setToken(requestToken(exchange, request.getToken()));
        return request;
    }

    private ApiModels.TokenRequest tokenRequest(HttpExchange exchange) throws IOException {
        ApiModels.TokenRequest request;
        if (isJson(exchange)) {
            request = jsonCodec.read(readAllBytes(exchange.getRequestBody()), ApiModels.TokenRequest.class);
        } else {
            request = new ApiModels.TokenRequest();
            request.setToken(params(exchange).get("token"));
        }
        request.setToken(requestToken(exchange, request.getToken()));
        return request;
    }

    private boolean isJson(HttpExchange exchange) {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private String requestToken(HttpExchange exchange, String fallback) {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        return fallback;
    }

    private Map<String, String> params(HttpExchange exchange) throws IOException {
        Map<String, String> params = new LinkedHashMap<>();
        parseParams(exchange.getRequestURI().getRawQuery(), params);
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            parseParams(body, params);
        }
        return params;
    }

    private void parseParams(String raw, Map<String, String> params) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (String pair : raw.split("&")) {
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : "";
            params.put(decode(key), decode(value));
        }
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is not supported.", e);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        send(exchange, status, jsonCodec.write(body));
    }

    private void sendText(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        send(exchange, status, body.getBytes(StandardCharsets.UTF_8));
    }

    private void serveAsset(HttpExchange exchange, String rawPath) throws IOException {
        if (rawPath.contains("..")) {
            send(exchange, 400, "Invalid asset path.".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String resourcePath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
        InputStream resource = HttpGameServer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource != null) {
            try (InputStream input = resource) {
                byte[] bytes = readAllBytes(input);
                if (isOneShotGif(resourcePath)) {
                    bytes = stripGifLoopExtension(bytes);
                }
                exchange.getResponseHeaders().set("Content-Type", contentType(resourcePath));
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                send(exchange, 200, bytes);
            }
            return;
        }

        File file = new File("src/main/resources", resourcePath);
        if (!file.isFile()) {
            send(exchange, 404, "Asset not found.".getBytes(StandardCharsets.UTF_8));
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentType(resourcePath));
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        byte[] bytes = Files.readAllBytes(file.toPath());
        if (isOneShotGif(resourcePath)) {
            bytes = stripGifLoopExtension(bytes);
        }
        send(exchange, 200, bytes);
    }

    private void serveResource(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        InputStream resource = HttpGameServer.class.getClassLoader().getResourceAsStream(resourcePath);
        byte[] bytes;
        if (resource != null) {
            try (InputStream input = resource) {
                bytes = readAllBytes(input);
            }
        } else {
            File file = new File("src/main/resources", resourcePath);
            if (!file.isFile()) {
                send(exchange, 404, "Page not found.".getBytes(StandardCharsets.UTF_8));
                return;
            }
            bytes = Files.readAllBytes(file.toPath());
        }
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        send(exchange, 200, bytes);
    }

    private String contentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        return type == null ? "application/octet-stream" : type;
    }

    private void send(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private boolean isOneShotGif(String path) {
        return path.endsWith(".gif") && path.contains("/animations/")
            && (path.contains("/dying_") || path.contains("_celebration_"));
    }

    private byte[] stripGifLoopExtension(byte[] bytes) {
        byte[] marker = "NETSCAPE2.0".getBytes(StandardCharsets.US_ASCII);
        byte[] stripped = bytes;
        int startIndex = 0;
        while (startIndex <= stripped.length - marker.length - 3) {
            int rangeStart = -1;
            int rangeEnd = -1;
            for (int i = startIndex; i <= stripped.length - marker.length - 3; i++) {
                if ((stripped[i] & 0xff) != 0x21 || (stripped[i + 1] & 0xff) != 0xff || (stripped[i + 2] & 0xff) != marker.length) {
                    continue;
                }
                boolean found = true;
                for (int j = 0; j < marker.length; j++) {
                    if (stripped[i + 3 + j] != marker[j]) {
                        found = false;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }

                int end = i + 3 + marker.length;
                while (end < stripped.length) {
                    int blockSize = stripped[end] & 0xff;
                    end++;
                    if (blockSize == 0) {
                        rangeStart = i;
                        rangeEnd = end;
                        break;
                    }
                    end += blockSize;
                }
                break;
            }

            if (rangeStart < 0) {
                return stripped;
            }
            stripped = withoutRange(stripped, rangeStart, rangeEnd);
            startIndex = rangeStart;
        }
        return stripped;
    }

    private byte[] withoutRange(byte[] bytes, int start, int end) {
        byte[] stripped = new byte[bytes.length - (end - start)];
        System.arraycopy(bytes, 0, stripped, 0, start);
        System.arraycopy(bytes, end, stripped, start, bytes.length - end);
        return stripped;
    }
}
