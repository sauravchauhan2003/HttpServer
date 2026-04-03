package org.example;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class RequestHandler {

    // ===== ROUTE STRUCT =====
    static class Route {
        Method method;

        Route(Method method) {
            this.method = method;
        }
    }

    // ===== ROUTE MAP =====
    private static Map<String, Route> routeMap = null;
    private static final Controller controller = new Controller();

    // ===== STATIC ROUTE BUILDER =====
    public static Map<String, Route> buildRoutes() {
        var methods = Controller.class.getDeclaredMethods();
        var map = new HashMap<String, Route>();

        for (var m : methods) {
            if (m.isAnnotationPresent(Endpoint.class)) {
                Endpoint e = m.getAnnotation(Endpoint.class);

                // validate signature
                if (m.getParameterCount() != 1 ||
                        m.getParameterTypes()[0] != HttpRequest.class) {
                    throw new RuntimeException("Invalid method: " + m.getName());
                }

                m.setAccessible(true);

                // key = METHOD:PATH
                String key = e.requesttype() + ":" + e.path();

                map.put(key, new Route(m));
            }
        }

        return map;
    }

    // ===== ENSURE ROUTES INITIALIZED =====
    private static void ensureRoutes() {
        if (routeMap == null) {
            synchronized (RequestHandler.class) {
                if (routeMap == null) {
                    routeMap = buildRoutes();
                }
            }
        }
    }

    // ===== MAIN HANDLER =====
    public static void handleRequest(Socket socket) {
        ensureRoutes(); // 🔥 important

        try {
            socket.setSoTimeout(30000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            OutputStream out = socket.getOutputStream();

            while (true) {

                StringBuilder requestBuilder = new StringBuilder();
                String line;

                try {
                    // read headers
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        requestBuilder.append(line).append("\n");
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }

                if (requestBuilder.isEmpty()) break;

                String rawRequest = requestBuilder.toString();

                try {
                    HttpRequest parsedRequest = parseRequest(rawRequest);

                    boolean keepAlive = !"close".equalsIgnoreCase(
                            parsedRequest.getHeaders()
                                    .getOrDefault("Connection", "keep-alive")
                    );

                    // ===== FAST ROUTING (O(1)) =====
                    String key = parsedRequest.getRequestType() + ":" + parsedRequest.getPath();
                    Route route = routeMap.get(key);

                    Object result = null;

                    if (route != null) {
                        result = route.method.invoke(controller, parsedRequest);
                    }

                    // ===== RESPONSE BUILD =====
                    HttpResponse response = new HttpResponse();

                    if (result == null) {
                        response.setHttpVersion("HTTP/1.1");
                        response.setStatus_code(404);
                        response.setStatusPhrase("Not Found");
                        response.setBody("Not Found");
                    }
                    else if (result instanceof HttpResponse) {
                        response = (HttpResponse) result;
                    }
                    else {
                        response.setHttpVersion("HTTP/1.1");
                        response.setStatus_code(200);
                        response.setStatusPhrase("OK");
                        response.setBody(result.toString());
                    }

                    // headers
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "text/plain");
                    headers.put("Content-Length",
                            String.valueOf(response.getBody() == null ? 0 : response.getBody().length()));
                    headers.put("Connection", keepAlive ? "keep-alive" : "close");

                    response.setHeaders(headers);

                    // send
                    out.write(response.toString().getBytes());
                    out.flush();

                    if (!keepAlive) break;

                } catch (Exception e) {
                    out.write((
                            "HTTP/1.1 400 Bad Request\r\n" +
                                    "Content-Length: 11\r\n" +
                                    "Connection: close\r\n\r\n" +
                                    "Bad Request").getBytes());
                    out.flush();
                    break;
                }
            }

        } catch (Exception ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    // ===== REQUEST PARSER =====
    public static HttpRequest parseRequest(String request) throws Exception {

        HttpRequest req = new HttpRequest();

        if (request == null || request.isEmpty()) {
            throw new Exception("Empty request");
        }

        String[] parts = request.split("\\r?\\n\\r?\\n", 2);
        String head = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        String[] lines = head.split("\r?\n");

        String[] requestLine = lines[0].split(" ");

        if (requestLine.length != 3) {
            throw new Exception("Malformed request line");
        }

        req.setRequestType(RequestType.valueOf(requestLine[0]));
        req.setPath(requestLine[1]);
        req.setHttpVersion(requestLine[2]);

        Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String l = lines[i];

            if (!l.contains(":")) {
                throw new Exception("Malformed header");
            }

            String[] h = l.split(":", 2);
            headers.put(h[0].trim(), h[1].trim());
        }

        req.setHeaders(headers);

        if (headers.containsKey("Content-Length")) {
            int len = Integer.parseInt(headers.get("Content-Length"));

            if (body.length() < len) {
                throw new Exception("Incomplete body");
            }

            req.setBody(body.substring(0, len));
        } else {
            req.setBody(body);
        }

        return req;
    }
}