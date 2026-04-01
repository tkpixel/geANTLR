# geANTLR

geANTLR is a JavaFX application built with Java 25.

## Requirements

To build and run this application locally, you need the following installed on your system:

*   **Java 25 (JDK)**: `openjdk-25-jdk` or equivalent. Ensure your `JAVA_HOME` environment variable is set to the Java 25 installation directory.
*   **Maven**: A modern version of Apache Maven for dependency management and building.

## Building the Executable JAR

This project is configured to use the `maven-shade-plugin` to build a "fat JAR" containing all required dependencies.

To build the executable JAR, open a terminal in the root directory of the project and run the following command:

```bash
mvn clean package
```

This will compile the code, run tests, and package the application into a single executable JAR file located in the `target/` directory. If you want to skip tests during the build, you can use:

```bash
mvn clean package -DskipTests
```

## Running the Application

After building the JAR, you can run the application from the command line. Because the application utilizes Java 25 preview features, you must include the `--enable-preview` flag when running the JAR.

Run the application with the following command:

```bash
java --enable-preview -jar target/geantlr-0.1.0.jar
```
