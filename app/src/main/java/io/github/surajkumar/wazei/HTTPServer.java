package io.github.surajkumar.wazei;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HTTPServer {

    public void create(String host, int port, HttpHandler handler) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.createContext("/", handler);
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Error starting the server", e);
        }
    }
}
