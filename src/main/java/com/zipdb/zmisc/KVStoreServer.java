package com.zipdb.zmisc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KVStoreServer {
    private final int port;
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public KVStoreServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("KVStoreServer listening on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line = in.readLine();
            if (line == null) return;

            String[] parts = line.split(" ", 3);
            String command = parts[0];

            if ("SET".equalsIgnoreCase(command) && parts.length == 3) {
                store.put(parts[1], parts[2]);
                out.write("OK\n");
            } else if ("GET".equalsIgnoreCase(command) && parts.length == 2) {
                String value = store.get(parts[1]);
                if (value != null) {
                    out.write("VALUE " + value + "\n");
                } else {
                    out.write("NOT_FOUND\n");
                }
            } else {
                out.write("ERROR Invalid command\n");
            }
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getStore() {
        return store;
    }
}

