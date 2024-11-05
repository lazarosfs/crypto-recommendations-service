# Crypto Recommendations Service

## Table of Contents

- [Description](#description)
- [Dependencies](#dependencies)
- [Building the Application](#building-the-application)
- [Running Tests](#running-tests)
- [Running the Application](#running-the-application)
- [Docker](#docker)
- [Endpoints](#endpoints)
- [Rate Limiting](#rate-limiting)
- [Swagger UI](#swagger-ui)
- [H2 Console](#h2-console)
- [Jacoco Coverage Report](#jacoco-coverage-report)
- [Contact](#contact)

## Description

This service provides recommendations and statistics for cryptocurrencies. It includes endpoints to retrieve crypto data
and uses rate limiting to prevent abuse.

The `CsvImportController::loadCsvFilesOnStartup` method loads initial data from included `src/main/resources/csv/*.csv`.

## Dependencies

- Spring Boot Starter Data JPA
- Spring Boot Starter Web
- SpringDoc OpenAPI Starter
- H2 Database
- Lombok
- Jacoco for code coverage
- Bucket4j for rate limiting

## Building the Application

To build the application, run the following command:

```
./mvnw clean package
```

## Running Tests

To run the tests, execute:

```
./mvnw test
```

## Running the Application

To run the application, you can either use the following command:

```
./mvnw spring-boot:run
```

or build the jar and run it with:

```
java -jar target/CryptoRecommendationsService-0.0.1-SNAPSHOT.jar
```

## Docker

To build the Docker image, use:

```
docker build -t crypto-recommendations-service .
```

To run the Docker container:

```
docker run -p 8080:8080 crypto-recommendations-service
```

## Endpoints

1. **Supported Cryptos**
    - **GET** `/api/crypto/supported`
    - Returns a list of supported cryptocurrency symbols.
    - Example command:
      ```
      curl -X GET "http://localhost:8080/api/crypto/supported"
      ```
1. **Normalized Range**
    - **GET** `/api/crypto/normalized-range`
    - Returns a sorted list of cryptos by normalized range.
    - Example command:
      ```
      curl -X GET "http://localhost:8080/api/crypto/normalized-range"
      ```

1. **Crypto Stats**
    - **GET** `/api/crypto/{symbol}/stats`
    - Retrieves the oldest, newest, minimum, and maximum prices for a specific cryptocurrency symbol.
    - Example command:
      ```
      curl -X GET "http://localhost:8080/api/crypto/BTC/stats"
      ```

1. **Highest Normalized Range**
    - **GET** `/api/crypto/highest-normalized-range?date={date}`
    - Retrieves the crypto with the highest normalized range for a specific date.
    - Example command:
      ```
      curl -X GET "http://localhost:8080/api/crypto/highest-normalized-range?date=2022-01-24"
      ```

1. **Post Crypto Data**
    - **POST** `/api/import/crypto`
    - Endpoint to additionally import cryptocurrency data from a CSV file (price with same crypto-timestamp combination
      will be overwritten).
    - Example command:
      ```
      curl -X POST "http://localhost:8080/api/import/crypto" -F "file=@./src/main/resources/csv/BTC_values.csv"
      ```

## Rate Limiting

Rate limiting is implemented using Bucket4j. The service allows 20 requests per minute per IP (set in RateLimitingFilter
class).

## Swagger UI

API documentation is available at: [Swagger UI](http://localhost:8080/swagger-ui/index.html)

## H2 Console

Access the H2 console for debugging at: [H2 Console](http://localhost:8080/h2-console) (no password required). Please
delete any ~/test.mv.db files before starting the app to make sure you are not re-using an existing H2 db.

## Jacoco Coverage Report

Jacoco is used for code coverage. The coverage report is automatically generated in `target/site/jacoco/index.html`.

## Contact

For any queries or issues, please contact lousou76@gmail.com
