package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Function;

public class Main {
    private HashMap<String, Function<HttpRequest,HttpRequest>> endpoints;

    private static volatile boolean running =true;
    public static void main(String[] args){

        try{
        ServerSocket serverSocket=new ServerSocket(8000);
        System.out.println("Started listening on port 8000");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                running = false;
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close(); // ✅ CLOSE PORT
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        while(running){
            try {
                Socket client = serverSocket.accept();
                RequestHandler.handleRequest(client);
                System.out.println("Request received from: " + client.getInetAddress());
                client.close();
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        }
        catch (IOException e){
            System.out.println("Port already in use");
        }

    }
}