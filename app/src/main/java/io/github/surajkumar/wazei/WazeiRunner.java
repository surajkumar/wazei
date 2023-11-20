package io.github.surajkumar.wazei;

public class WazeiRunner {
    private static final String HOST = System.getenv().getOrDefault("host", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("port", "8080"));

    public static void main(String[] args) throws Exception {
        Wazei.init(HOST, PORT);
    }
}
