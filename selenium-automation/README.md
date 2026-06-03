# Selenium Automation Framework

This Maven project covers the Selenium automation framework tasks:

- T063: Setup Maven project for automation
- T064: Add Selenium and TestNG dependencies
- T064: Configure Chrome WebDriver through Selenium Manager
- T065: Setup project structure using Page Object Model
- T066: Identify locators with name and CSS selectors
- T067: Implement basic register and login navigation tests

It is configured for Selenium WebDriver, TestNG, Maven Surefire, and the `testng.xml` suite file.

## Prerequisites

- Java 21 or newer
- Maven installed and available as `mvn`
- The ecommerce frontend running on `http://localhost:5173`
- The Spring Boot backend running on `http://localhost:8082`
- MySQL running with the database credentials from `backend/src/main/resources/application.properties`

## Run Tests

```powershell
cd D:\Front-end\selenium-automation
mvn test
```

Use a different browser:

```powershell
mvn test -Dbrowser=edge
```

Run headless:

```powershell
mvn test -Dheadless=true
```

Use a different website URL:

```powershell
mvn test -DbaseUrl=http://localhost:5173
```
