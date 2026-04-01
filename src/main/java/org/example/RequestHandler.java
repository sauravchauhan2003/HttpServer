package org.example;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler {

    public static void handleRequest(Socket socket){
        try{
            socket.setSoTimeout(30000); // 30 sec timeout

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            while(true){
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                try {
                    // 🔥 READ HEADERS
                    while((line = bufferedReader.readLine()) != null && !line.isEmpty()){
                        stringBuilder.append(line).append("\n");
                    }
                } catch (SocketTimeoutException e) {
                    // ✅ NORMAL: client idle, close connection
                    System.out.println("Client idle timeout → closing connection");
                    break;
                }

                // If no request, break
                if(stringBuilder.isEmpty()){
                    break;
                }

                String rawRequest = stringBuilder.toString();

                try{
                    HttpRequest parsedRequest = parseRequest(rawRequest);
                    System.out.println("RAW REQUEST:\n" + rawRequest);

                    // 🔥 Check keep-alive from client
                    boolean keepAlive = !"close".equalsIgnoreCase(
                            parsedRequest.getHeaders().getOrDefault("Connection", "keep-alive")
                    );

                    String response =
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: 13\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                                    "\r\n" +
                                    "Hello, World!";

                    bufferedWriter.write(response);
                    bufferedWriter.flush();

                    // 🔥 If client wants to close → exit loop
                    if(!keepAlive){
                        break;
                    }

                }
                catch (Exception e) {
                    String response =
                            "HTTP/1.1 400 Bad Request\r\n" +
                                    "Content-Length: 11\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n" +
                                    "Bad Request";

                    bufferedWriter.write(response);
                    bufferedWriter.flush();
                    break;
                }
            }

        }
        catch (Exception e){
            // ❌ No more timeout noise here
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public static HttpRequest parseRequest (String request) throws Exception{
        HttpRequest request1 = new HttpRequest();

        if(request == null || request.isEmpty()){
            throw new Exception("Empty request");
        }

        String[] parts = request.split("\\r?\\n\\r?\\n", 2);
        String head = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        String[] lines = head.split("\r?\n");

        if (lines.length == 0) {
            throw new Exception("Malformed request: Missing request line");
        }

        String[] requestLine = lines[0].split(" ");

        if (requestLine.length != 3) {
            throw new Exception("Malformed request line");
        }

        request1.setPath(requestLine[1]);
        request1.setHttpVersion(requestLine[2]);

        // 🔹 Parse Headers
        Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            if (!line.contains(":")) {
                throw new Exception("Malformed header: " + line);
            }

            String[] headerParts = line.split(":", 2);

            String key = headerParts[0].trim();
            String value = headerParts[1].trim();

            headers.put(key, value);
        }

        request1.setHeaders(headers);

        // 🔹 Handle Body
        if (headers.containsKey("Content-Length")) {
            int contentLength;

            try {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                throw new Exception("Invalid Content-Length");
            }

            if (body.length() < contentLength) {
                throw new Exception("Body shorter than Content-Length");
            }

            request1.setBody(body.substring(0, contentLength));
        } else {
            request1.setBody(body);
        }

        System.out.println("Parsing request:\n" + request);

        return request1;
    }
}