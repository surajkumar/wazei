package io.github.surajkumar.wazei;

import com.sun.net.httpserver.Headers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.handler.Handler;
import dev.mccue.microhttp.handler.IntoResponse;
import dev.mccue.microhttp.json.JsonResponse;

import io.github.surajkumar.wazei.config.Config;
import io.github.surajkumar.wazei.config.ConfigSearcher;

import org.microhttp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Map;

public class MicroHttpHandler implements Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroHttpHandler.class);
    private final ConfigSearcher configSearcher;

    public MicroHttpHandler(ConfigSearcher configSearcher) {
        this.configSearcher = configSearcher;
    }

    @Override
    public IntoResponse handle(Request request) {
        String path = request.uri();

        LOGGER.info("Received request from path: {}", request.uri());

        try {
            Config config = configSearcher.getConfigForPath(path);
            if (config == null) {
                return new JsonResponse(404, null);
            }

            String method = request.method();
            Map<String, String> queryParameters =
                    QueryStringParser.getParametersFromQueryString(URI.create(request.uri()));
            byte[] body = request.body();

            Headers headers = new Headers();
            request.headers().forEach(h -> headers.add(h.name(), h.value()));

            RequestProcessor processor = new RequestProcessor(config);
            MethodResponse response =
                    processor.processRequest(
                            path, method, queryParameters, headers, new ByteArrayInputStream(body));

            if (response != null) {
                if (response.containsResponse()) {
                    System.out.println("OK JOHm");

                    return new JsonResponse(200, null);
                } else {
                    return new JsonResponse(200, Json.objectBuilder().toJson());
                }
            } else {
                return new JsonResponse(400, Json.objectBuilder().toJson());
            }

        } catch (Exception e) {
            LOGGER.error("Error processing request", e);
            return new JsonResponse(500, Json.objectBuilder().toJson());
        }
    }
}
