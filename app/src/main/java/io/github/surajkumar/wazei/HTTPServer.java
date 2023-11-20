package io.github.surajkumar.wazei;

import com.sun.net.httpserver.HttpServer;

import io.github.surajkumar.wazei.config.Config;
import io.github.surajkumar.wazei.config.ConfigSearcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class HTTPServer {
    private final ConfigSearcher configSearcher;

    public HTTPServer(List<Config> configList) {
        this.configSearcher = new ConfigSearcher(configList);
    }

    public void create(String host, int port) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.createContext("/", new RequestHandler(configSearcher));
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Error starting the server", e);
        }
    }
}
