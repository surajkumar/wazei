package io.github.surajkumar;

/**
 * This class contains examples of when using the WazeiRunnerNoConfig.
 *
 * To call any of the test methods try the following syntax:
 *
 * http://localhost:8080/ExampleNoDocsController/greet?name=John
 *
 * Note: Since there is no strict parsing, you cannot handle headers using this method.
 */
public class ExampleNoDocsController {

    public String greet(String name) {
        return "Hello, %s".formatted(name);
    }

    public void print() {
        System.out.println("Hello, World!");
    }

    public User getUser() {
        return new User("John", "Smith", 65, "London");
    }

}
