package io.github.surajkumar.wazei;

public enum HTTPStatusCode {
    OK(200, "OK"),
    NOT_FOUND(404, "Page Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    private final int code;
    private final String value;

    HTTPStatusCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
