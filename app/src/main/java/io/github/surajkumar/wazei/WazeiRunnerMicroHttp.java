package io.github.surajkumar.wazei;

import io.github.surajkumar.wazei.config.ConfigLoader;
import io.github.surajkumar.wazei.config.ConfigSearcher;

import org.microhttp.EventLoop;
import org.microhttp.NoopLogger;
import org.microhttp.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WazeiRunnerMicroHttp {
    private static final Logger LOGGER = LoggerFactory.getLogger(WazeiRunnerMicroHttp.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting server with MicroHttp");

        ConfigSearcher configSearcher = new ConfigSearcher(ConfigLoader.loadConfig());

        EventLoop eventLoop =
                new EventLoop(
                        OptionsBuilder.newBuilder().withPort(8080).build(),
                        NoopLogger.instance(),
                        (request, callback) ->
                                Thread.startVirtualThread(
                                        () ->
                                                new MicroHttpHandler(configSearcher)
                                                        .handle(request)));
        eventLoop.start();
        eventLoop.join();
    }
}
