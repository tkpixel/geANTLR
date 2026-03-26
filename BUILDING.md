# Building geANTLR

geANTLR uses Maven for dependency management and building.

## Requirements
*   **Java 25:** This project requires JDK 25 to compile and run. Ensure your `JAVA_HOME` is set appropriately or your system's default Java is version 25.
*   **Maven:** A modern version of Apache Maven.

## Building the Executable JAR
To create a standalone, executable "fat JAR" containing all required dependencies, run the following command in the project root directory:

```bash
mvn clean package
```

This uses the `maven-shade-plugin` to package everything into a single JAR file located in the `target/` directory.

## Running the Application
Once the build has completed successfully, you can launch the application using standard Java commands.

Since JavaFX is modularized but bundled into a single shaded JAR in this setup, run it with the following command:

```bash
java --enable-preview -jar target/geantlr-0.1.0.jar
```
