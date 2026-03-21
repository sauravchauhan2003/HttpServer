package org.example;

import java.io.*;
import java.net.Socket;

public class RequestHandler {

    public static void handleRequest(Socket socket){
        try{
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while(true){
                StringBuilder stringBuilder=new StringBuilder();
                String line;
                while((line=bufferedReader.readLine())!=null && !line.isEmpty()){
                    stringBuilder.append(line).append("\n");
                }
                if(stringBuilder.isEmpty()){
                    break;
                }
                System.out.println(stringBuilder);
                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Length: 13\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Connection: keep-alive\r\n" +
                                "\r\n" +
                                "Hello, World!";

                bufferedWriter.write(response);
                bufferedWriter.flush();
            }
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
