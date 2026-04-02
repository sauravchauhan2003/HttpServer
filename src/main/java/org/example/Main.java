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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

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
                Thread.startVirtualThread(()->{
                    RequestHandler.handleRequest(client);
                });

            } catch (IOException e) {
                if (running) {

                }
            }

        }
        }
        catch (IOException e){
            System.out.println("Port already in use");
        }

    }
}