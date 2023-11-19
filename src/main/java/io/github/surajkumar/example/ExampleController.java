package io.github.surajkumar.example;

public class ExampleController {

    /**
     * ---
     * $method=get
     * $path=/john
     * $content-type=application/json
     * ---
     */
    public User showUser() {
        return new User("John", "Doe", 31, "London");
    }

    /**
     * ---
     * $method=post
     * $path=/create
     * ---
     */
    public void createUser(User userBody) {
        System.out.printf("Name: %s %s%n", userBody.getForename(), userBody.getSurname());
        System.out.printf("Age: %d%n", userBody.getAge());
        System.out.printf("Location: %s%n", userBody.getLocation());
    }

    /**
     * ---
     * $method=get
     * $path=/hello
     * $content-type=application/text
     * ---
     */
    public String hello(String nameParam) {
        return "Hello, %s!".formatted(nameParam);
    }

    /**
     * ---
     * $method=get
     * $path=/ping
     * $content-type=application/text
     * ---
     */
    public String ping() {
        return "Pong";
    }

}
