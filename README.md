# Wazei: Rapid REST API Framework
Wazei Framework is a versatile web framework designed for the swift creation of REST APIs, particularly useful for testing and generating stubs. You can develop REST APIs rapidly without coupling your code to this framework.
This decoupling ensures seamless integration for those who aim to use a real framework in the future. Your source code is remains fully portable with this no-code integration solution.

## Overview
This repository provides the necessary code to facilitate the accelerated creation of REST APIs, enabling you to work independently of your source code. This is advantageous for scenarios where you may not have finalized the choice of library for your application or when you need to quickly generate a REST API for testing or stubbed data.

### When to Use:
1. **Library Agnosticism**: When you're uncertain about the library you'll use for your application, Wazei allows you to write controllers, and by leveraging code documentation, you can effortlessly create your endpoints.
2. **Rapid API Prototyping**: When you want to swiftly create a REST API for testing purposes or to generate stubbed data.

## How to use

1. **Create a Package for Controllers:**
   Start by creating a package to house your controllers.


2. **Define a Controller Class:**
   Create a controller class with your desired logic. Use the meta-block to document the code for endpoint creation.
```java
package com.example;

public class HelloController {
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
}
```
3. **Run the Application:**
   Create a main method to run the application:

```java
import io.github.surajkumar.wazei.Wazei;

public class Main {
    public static void main(String[] args) throws Exception {
        Wazei.init("localhost", 8080);
    }
}
```
4. **Build and Run:**
   Run `gradle build` to initiate parsing and initialization at **Compile Time**. After building, generate the `.jar` using `gradle jar`.

## [See Example](examples/src/main/java/io/github/surajkumar/ExampleController.java)


## Disclaimer
Please be aware that this repository is still in its early stages of development. Continuous enhancements and improvements will be made over time to align with project needs and requirements.
## Contributions
Contributions to this project are welcome. If you'd like to contribute, please follow these steps:

1. Fork the repository.
2. Create a dedicated branch for your feature or bug fix.
3. Implement your changes and include tests if applicable.
4. Submit a pull request.
