# GEMINI.md - `other-service-api`

This document provides a comprehensive overview of the `other-service-api` project, its purpose, architecture, and instructions for development and deployment.

## Project Overview

The `other-service-api` is a Java-based microservice built using the Spring Boot framework. It is part of the larger "Auto Adda" ecosystem. The primary responsibility of this service is to manage "Other Services," which appear to be services offered by garages or other entities. It provides a RESTful API for creating, retrieving, updating, deleting (CRUD), and searching for these services.

The service integrates with a service discovery agent (Eureka) and uses Feign for declarative REST client communication with other internal services like an authentication service.

## Architecture and Technology

-   **Language:** Java 21
-   **Framework:** Spring Boot 3
-   **Build Tool:** Apache Maven
-   **Core Components:**
    -   **Spring Web:** For creating RESTful APIs.
    -   **Spring Data JPA:** For database interaction.
    -   **Spring Cloud:**
        -   **Eureka Client:** For service registration and discovery.
        -   **OpenFeign:** For resilient HTTP client communication.
-   **Database:** The project is configured to work with PostgreSQL and MySQL.
-   **API Documentation:** OpenAPI (Swagger) is integrated for automatic API documentation. The UI can typically be accessed at `/swagger-ui.html`.
-   **Development Utilities:**
    -   **Lombok:** To reduce boilerplate code (e.g., getters, setters, constructors).
    -   **MapStruct:** For automated bean mapping.

## Building and Running the Project

### Prerequisites

-   Java 21
-   Apache Maven
-   Access to a running instance of a PostgreSQL or MySQL database.
-   Access to a running Eureka server for service discovery.

### Commands

1.  **Build the project:**
    This command compiles the source code, runs tests, and packages the application into a JAR file.

    ```bash
    mvn clean install
    ```

2.  **Run the application:**
    This command starts the Spring Boot application. The service will attempt to connect to the configured database and register with the Eureka server.

    ```bash
    mvn spring-boot:run
    ```

    Alternatively, you can run the packaged JAR file:

    ```bash
    java -jar target/OtherServiceApplication-0.1.jar
    ```

## Development Conventions

-   **API Design:** The project follows a standard RESTful API design. The main endpoints are exposed via the `OtherServiceController` under the `/other-services` base path.
-   **Authentication/Authorization:** Most endpoints require a custom header, `X-Auto-Adda-Id`, which likely identifies the user or tenant making the request.
-   **Configuration:** Application configuration is managed in `src/main/resources/application.properties`. For local development, settings can be overridden in `application-local.properties`. This is where database credentials and Eureka server URLs should be configured.
-   **Code Style:** The codebase makes extensive use of Lombok annotations (`@RequiredArgsConstructor`, `@Data`, etc.) and the MapStruct library for mapping between entities and DTOs.
-   **Testing:** The project includes unit/integration tests using JUnit and Mockito. Tests are located in the `src/test/java` directory.
