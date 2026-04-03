package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        RequestHandler.buildRoutes();
        try {
            ServerSocket serverSocket = new ServerSocket(8000);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running = false;
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            while (running) {
                try {
                    Socket client = serverSocket.accept();

                    Thread.startVirtualThread(() -> {
                        RequestHandler.handleRequest(client);
                    });

                } catch (IOException e) {
                    if (running) {
                        // ignore
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Port already in use");
        }
    }
}