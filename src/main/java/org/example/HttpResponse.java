package org.example;

import java.util.List;
import java.util.Map;

public class HttpResponse {
    private int status_code;
    private String body;
    private String httpVersion;
    private Map<String,String> headers;
    private String statusPhrase;

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getStatusPhrase() {
        return statusPhrase;
    }

    public void setStatusPhrase(String statusPhrase) {
        this.statusPhrase = statusPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();

        // Status Line
        response.append(httpVersion)
                .append(" ")
                .append(status_code)
                .append(" ")
                .append(statusPhrase)
                .append("\r\n");

        // Headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                response.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\r\n");
            }
        }

        // Blank line to separate headers and body
        response.append("\r\n");

        // Body
        if (body != null) {
            response.append(body);
        }

        return response.toString();
    }
}
