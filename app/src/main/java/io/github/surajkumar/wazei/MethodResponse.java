package io.github.surajkumar.wazei;

public class MethodResponse {
    private final boolean hasResponse;
    private Object response;

    public MethodResponse() {
        this.hasResponse = false;
    }

    public MethodResponse(Object response) {
        this.response = response;
        this.hasResponse = true;
    }

    public boolean containsResponse() {
        return hasResponse;
    }

    public Object getResponse() {
        return response;
    }
}
