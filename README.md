# Lightweight Java HTTP Server

A custom-built, lightweight, multi-threaded HTTP Server implemented in pure Java from scratch. It utilizes Java Virtual Threads to handle concurrent connections efficiently. It also offers a simple annotation-based routing mechanism, similar to Spring Boot, allowing developers to define API endpoints cleanly.

## Features
- **Zero Dependencies**: Built entirely using standard Java Libraries (`java.net`, `java.io`, `java.lang.reflect`). No external frameworks required.
- **Annotation-Based Routing**: Define endpoints elegantly using custom `@Endpoint` annotations.
- **High Concurrency**: Utilizes lightweight Java Virtual Threads for robust concurrent request handling.
- **Keep-Alive Support**: Reuses socket connections across multiple requests for better overall performance.
- **Graceful Shutdown**: Intercepts JVM shutdown to gracefully close server sockets safely.

## Architecture & Components
The project is structured into clean components:
- **`Main.java`**: The entry point. It initializes server routes and opens a ServerSocket on a specified port (default: 8000). Incoming sockets are handled asynchronously using Virtual Threads.
- **`RequestHandler.java`**: The core routing engine. 
  - Scans `Controller.java` to map URL endpoints to handler functions upon initialization using Java Reflection.
  - Parses raw HTTP byte streams into `HttpRequest` objects.
  - Matches requests to predefined routes and constructs standard `HttpResponse` outputs, writing them back to the client.
- **`HttpRequest.java` / `HttpResponse.java`**: Represents HTTP structures with properties for method, URI path, HTTP version, headers, and body.
- **`@Endpoint` Annotation & `RequestType.java`**: Defines an annotation that allows methods in the `Controller` class to specify their respective path and HTTP method type natively. 

## How to create endpoints
Endpoints are mapped inside the `Controller.java` class using the custom `@Endpoint` annotation. 

Here is an example of creating a GET request to `/hello`:
```java
package org.example;

public class Controller {
    
    @Endpoint(path = "/hello", requesttype = RequestType.GET)
    public String sayHello(HttpRequest request) {
        return "Hello World!";
    }

    @Endpoint(path = "/data", requesttype = RequestType.POST)
    public HttpResponse createData(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setStatus_code(201);
        response.setStatusPhrase("Created");
        response.setBody(request.getBody());
        return response;
    }
}
```

*Note: The mapped method MUST accept a single parameter of type `HttpRequest`. It can return a `String` (which will be automatically wrapped in an OK 200 HTTP response) or a custom `HttpResponse` object for fine-grained control.*

## Running the Server
The application uses Maven. You can compile and start the server natively.

### Prerequisites
- Java 21 or higher (Required for Virtual Threads capability)
- Maven

### Build & Run
1. Navigate to the root directory `HttpServer`.
2. Clean and compile the project:
   ```bash
   mvn clean install
   ```
3. Run the application (Main class `org.example.Main`).
   The server will bind and listen on port `8000`.

## Testing the Server
You can test the server simply by using standard `curl` commands matching the configured paths in the `Controller`. 
```bash
curl http://localhost:8000/hello
```
