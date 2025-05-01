package com.zipdb.zmisc;

import java.io.*;
import java.net.Socket;

public class KVStoreClient {

    public static String sendCommand(String host, int port, String command) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write(command + "\n");
            out.flush();
            return in.readLine();

        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public static String set(String host, int port, String key, String value) {
        return sendCommand(host, port, "SET " + key + " " + value);
    }

    public static String get(String host, int port, String key) {
        return sendCommand(host, port, "GET " + key);
    }
}
