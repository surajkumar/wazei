package io.github.surajkumar;

import java.util.HashMap;
import java.util.Map;

public class CustomHeadersAndCodeExampleController {

    /**
     * ---
     * $method=get
     * $path=/example
     * $content-type=application/text
     * ---
     */
    public Example example() {
        Example example = new Example();
        example.setHttpStatusCode(201);

        Map<String, String> headers = new HashMap<>();
        headers.put("testHeader", "testHeaderValue");
        example.setHttpHeaders(headers);

        return example;
    }


}
