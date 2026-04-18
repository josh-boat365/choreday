# ChoreDay - AI Agent Customization Guide

This document helps AI agents understand and contribute effectively to the **ChoreDay** JavaFX desktop application.

## Project Overview

**ChoreDay** is a student planner application built with JavaFX that integrates weather API data to help users manage chores based on weather conditions. It features user authentication, a chore dashboard, and real-time weather integration using the Open-Meteo API.

- **Framework**: JavaFX 17.0.14 (MVC desktop application)
- **Build System**: Maven 3.0+
- **Language**: Java (OpenJDK 17)
- **Database**: MySQL (choreday database)
- **Key Dependencies**: Lombok, Apache Commons DBCP2, MySQL JDBC, org.json

## Quick Start Commands

| Purpose | Command |
|---------|---------|
| **Build & Run** | `mvn clean javafx:run` |
| **Compile Only** | `mvn compile` |
| **Run Tests** | `mvn test` |
| **Package JAR** | `mvn package` |
| **Clean Artifacts** | `mvn clean` |

**Prerequisites**: MySQL running locally with a `choreday` database, username `choreday`, password `1234`.

## Architecture & Design Patterns

### MVC Structure

```
Controllers/         → UI logic & event handling (3 controllers)
Services/           → Business logic & data operations (4 services)
Models/             → Data entities with Lombok (4 models)
Utils/              → Singletons & helpers (3 utilities)
Resources (FXML)    → UI markup & styling
```

### Key Patterns

| Pattern | Implementation | Usage |
|---------|---|---|
| **Singleton** | [DatabaseConnectionPool.java](src/main/java/org/group45/choreday/utils/DatabaseConnectionPool.java), [SessionManager.java](src/main/java/org/group45/choreday/utils/SessionManager.java) | Global connection pool & user session management |
| **MVC** | Controllers bind to FXML; services handle logic | Controllers call services, never direct DB queries |
| **Builder** | Lombok `@Builder` on all models | Fluent object construction (e.g., `UserModel.builder().studentId("S001").build()`) |
| **Service Layer** | Services encapsulate DB & API calls | Clear separation of UI from business logic |
| **Threading** | `new Thread()` + `Platform.runLater()` for UI updates | Prevent blocking during DB/API calls |

### Module Breakdown

#### Controllers
- [**LoginController**](src/main/java/org/group45/choreday/controllers/LoginController.java): Handles sign-in/sign-up navigation
- [**SignUpController**](src/main/java/org/group45/choreday/controllers/SignUpController.java): User registration with validation
- [**DashboardController**](src/main/java/org/group45/choreday/controllers/DashboardController.java): Main app screen; displays chores & weather

#### Services
- [**SignInService**](src/main/java/org/group45/choreday/services/SignInService.java): User authentication against database
- [**SignUpService**](src/main/java/org/group45/choreday/services/SignUpService.java): User registration (password handling, validation)
- [**ChoreService**](src/main/java/org/group45/choreday/services/ChoreService.java): CRUD operations for chores; manages weather-chore relationships
- [**WeatherService**](src/main/java/org/group45/choreday/services/WeatherService.java): External API integration (Open-Meteo); geocoding & weather fetch

#### Models
- [**UserModel**](src/main/java/org/group45/choreday/models/UserModel.java): Student ID, name, password
- [**ChoreModel**](src/main/java/org/group45/choreday/models/ChoreModel.java): Activity, location, weather conditions, timestamps
- [**WeatherRecord**](src/main/java/org/group45/choreday/models/WeatherRecord.java): Temperature, humidity, wind speed, UV index
- [**WeatherResponse**](src/main/java/org/group45/choreday/models/WeatherResponse.java): API response wrapper for JSON parsing

#### Utils
- [**DatabaseConnectionPool**](src/main/java/org/group45/choreday/utils/DatabaseConnectionPool.java): Apache Commons DBCP2 connection pooling; methods: `getConnection()`, `closeConnection()`, `closePool()`
- [**SessionManager**](src/main/java/org/group45/choreday/utils/SessionManager.java): In-memory user session; methods: `setCurrentUser()`, `getCurrentUser()`, `logout()`
- [**Navigator**](src/main/java/org/group45/choreday/utils/Navigator.java): Scene transition utility; handles FXML loading and window resizing

## Coding Conventions

### Module Declaration
The project uses Java 17 modules (JPMS). [module-info.java](src/main/java/module-info.java) declares dependencies and module visibility:
```java
module org.group45.choreday {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires org.apache.commons.dbcp2;
    requires jakarta.persistence;
    requires org.json;

    opens org.group45.choreday to javafx.fxml;
    opens org.group45.choreday.controllers to javafx.fxml;

    exports org.group45.choreday;
    exports org.group45.choreday.controllers;
    exports org.group45.choreday.utils;
    exports org.group45.choreday.services;
    exports org.group45.choreday.models;
}
```
**Key Patterns**:
- Use `requires transitive` for foundational modules (javafx.graphics, java.sql) that dependents may also need
- Use `opens` to allow JavaFX reflection access to controller packages (needed for FXML injection via `@FXML`)
- Use `exports` to define public API for each package
- **Do NOT require compile-time only dependencies** (scope=provided in pom.xml, like Lombok) in module-info.java; instead configure them in Maven's compiler with `--add-reads org.group45.choreday=ALL-UNNAMED` to allow reading from the unnamed module
- Always add new runtime dependencies to module-info.java before using them in code

### Lombok Annotations
All models use `@Builder` for fluent construction. Most use `@Data` (generates getters, setters, equals, hashCode, toString); `UserModel` uses `@Getter/@Setter` explicitly:
```java
// Standard pattern (ChoreModel, WeatherRecord, WeatherResponse)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChoreModel { ... }

// UserModel pattern (explicit getters/setters)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel { ... }
```

### FXML Binding in Controllers
Controllers inject UI components via `@FXML`. The `@FXML public void initialize()` method is called automatically after FXML loads:
```java
@FXML private TextField studentIdField;
@FXML private Button signInButton;

@FXML
public void initialize() {
    // Called automatically after FXML components are injected
    signInButton.setOnAction(event -> handleLogin());
}

@FXML
private void handleLoginButtonClick(ActionEvent event) { ... }
```
**Key Pattern**: Set up event listeners and initialize UI state in `initialize()`, not in constructors.

### Threading & UI Updates
Long-running operations (DB/API) must run on separate threads; UI updates must return to JavaFX thread:
```java
new Thread(() -> {
    UserModel user = signInService.signIn(credentials);
    Platform.runLater(() -> {
        // Update UI safely here
        dashboard.show();
    });
}).start();
```

### Database Operations
Use try-with-resources for auto-closing connections & statements. Always check null results before using:
```java
// Simple query (SignInService pattern)
try (Connection conn = DatabaseConnectionPool.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, studentId);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        return UserModel.builder().studentId(rs.getString("student_id")).build();
    }
} catch (SQLException e) {
    e.printStackTrace();
    return null;
}

// Transaction with rollback (ChoreService pattern)
try (Connection conn = DatabaseConnectionPool.getConnection()) {
    conn.setAutoCommit(false);
    try {
        // Multiple statements here
        stmt.executeUpdate();
        conn.commit();
        return true;
    } catch (SQLException e) {
        conn.rollback();
        e.printStackTrace();
        return false;
    }
} catch (SQLException e) { ... }
```
**Pattern**: Services return `null` or `false` on error; controllers must always null-check results before using them.

### Alert Dialog Pattern
Common across all controllers:
```java
showAlert(Alert.AlertType.ERROR, "Title", "Error message");
```

### Navigation Pattern
Use [Navigator.java](src/main/java/org/group45/choreday/utils/Navigator.java) for all scene transitions:
```java
// Default 800x600 window
Navigator.navigateTo("Dashboard.fxml", currentNode, null, null);

// Custom dimensions
Navigator.navigateTo("Dashboard.fxml", currentNode, 1024, 768);
```
**Key Details**: Pass the source UI node to get the current Stage; `width` and `height` default to 800x600 if null. Navigator loads FXML from package root: `/org/group45/choreday/`.

## Database Schema

**MySQL** with three main tables:

| Table | Key Fields | Purpose |
|-------|---|---|
| `users` | `student_id` (PK), `full_name`, `password` | User accounts |
| `weather_records` | `id` (PK), `temperature`, `humidity`, `wind_speed`, `uv_index`, `city`, `country` | Weather data from Open-Meteo API |
| `chores` | `id` (PK), `activity_name`, `city`, `weather_id` (FK), `student_id` (FK), `created_at` | User chores linked to weather |

**Connection Details**: `localhost:3306` (default MySQL port), database `choreday`, user `choreday`, password `1234`

## Critical Files & Patterns to Learn

| File | Key Learning |
|------|---|
| [DashboardController.java](src/main/java/org/group45/choreday/controllers/DashboardController.java) | Threading, service composition, complex UI updates |
| [ChoreService.java](src/main/java/org/group45/choreday/services/ChoreService.java) | Transaction management, multi-step DB operations, JOINs |
| [WeatherService.java](src/main/java/org/group45/choreday/services/WeatherService.java) | HTTP requests, JSON parsing with `org.json`, error handling |
| [DatabaseConnectionPool.java](src/main/java/org/group45/choreday/utils/DatabaseConnectionPool.java) | Singleton pattern, static initialization, resource management |
| [SessionManager.java](src/main/java/org/group45/choreday/utils/SessionManager.java) | Singleton for cross-controller state |

## Common Development Tasks

### Adding a New Feature
1. **Create/extend Service** → Add business logic method in appropriate service
2. **Update Controller** → Add UI event handler; call service on background thread; use `Platform.runLater()` for UI updates
3. **Update FXML** → Add UI components to relevant `.fxml` file
4. **Database (if needed)** → Add table/columns and update service queries

### UI/UX Best Practices - ChoreDay Color Scheme
The application uses a modern, professional theme with these key colors:
- **Primary Dark**: `#232323` (dark backgrounds, selected rows)
- **Primary Blue**: `#007AFF` (focused inputs, interactive elements)
- **Error Red**: `#FF3B30` (invalid inputs, errors)
- **Light Gray**: `#F8F9FA` (main background)
- **Border Gray**: `#E0E0E0` (input borders)
- **Text Dark**: `#333333` (primary text)
- **Text Light**: `#9E9E9E` (secondary/placeholder text)

**Alert Messages**: Use emoji prefixes for better UX:
- ✓ for success (Information alerts)
- ✗ for errors (Error alerts)
- ⚠ for warnings (Warning alerts)

Example: `"✓ Account Created!"`, `"✗ Login Failed"`, `"⚠ Missing Information"`

**Input Validation Pattern**:
1. Check for empty fields first
2. Validate field length/format
3. Show specific, actionable error messages
4. Clear problematic fields and request focus for retry

See [LoginController.handleSignIn()](src/main/java/org/group45/choreday/controllers/LoginController.java) for validation pattern.

### Adding a New Controller-View
1. Create new `.fxml` file in `src/main/resources/org/group45/choreday/`
2. Create controller class in `src/main/java/org/group45/choreday/controllers/` with `@FXML` bindings
3. Register FXML filename in controller's `getResource()` path
4. Use `Navigator.navigateTo()` to link from existing screens

### Managing User Sessions & Logout
`SessionManager` stores the current user globally. To implement logout:
```java
// On logout button/action
Optional<ButtonType> result = logoutAlert.showAndWait();
if (result.isPresent() && result.get() == ButtonType.OK) {
    SessionManager.setCurrentUser(null);  // Clear session
    Navigator.navigateTo("SignIn.fxml", profileIcon, null, null);  // Return to login
}
```
See [DashboardController.initialize()](src/main/java/org/group45/choreday/controllers/DashboardController.java) lines 119-130 for full example.

### Database Changes
1. Execute SQL against `choreday` database
2. Update related service methods with new queries
3. Optionally update model classes to match new schema

## Potential Pitfalls & Solutions

| Pitfall | Solution | Example |
|---------|----------|---------|
| **UI blocking during DB calls** | Always use `new Thread()` + `Platform.runLater()` for DB/API operations | [DashboardController.addChore()](src/main/java/org/group45/choreday/controllers/DashboardController.java) lines 153-179 |
| **Hardcoded DB credentials** | Currently in [DatabaseConnectionPool.java](src/main/java/org/group45/choreday/utils/DatabaseConnectionPool.java) lines 10-15; consider moving to `.env` or config file | Set `CHOREDAY_DB_USER`, `CHOREDAY_DB_PASS` env vars |
| **No input validation on UI** | Add validation in controller before calling service; services should also validate inputs | Check for empty fields in `addChore()` before service call |
| **Null returns from services on error** | Services return `null` on failure; **always null-check before using results** | See [LoginController](src/main/java/org/group45/choreday/controllers/LoginController.java) line 44: `if (user != null)` |
| **Weather API calls every time chore added** | No caching; consider adding `WeatherRecord` cache or checking if city weather exists in DB | Could query `weather_records` before API call in `ChoreService.saveChore()` |
| **No result set column validation** | Always handle missing columns gracefully (use `optString()`, `optInt()` or check with `has()`) | See [WeatherService.java](src/main/java/org/group45/choreday/services/WeatherService.java) line 49-50 |
| **Basic error handling** | Only console stack traces; consider adding SLF4J/Logback for better logging | Add `e.printStackTrace()` replacement or logging framework |
| **Manual JDBC** | No ORM framework; for complex queries, consider JPA/Hibernate in future versions | Current JDBC is fine for this scope; revisit if >10 entities |

## Testing

**Current State**: No test directory exists yet (`src/test/java/` not created). Infrastructure is configured in `pom.xml` (JUnit 5.12.1).

**To Add Tests**:
1. Create `src/test/java/org/group45/choreday/` directory structure
2. Write tests for service layer (ChoreService, WeatherService, SignInService, SignUpService)
3. Run with `mvn test`
4. Consider testing: DB operations, API calls, error handling in services

## External Dependencies

### Open-Meteo API (Weather)
- **Geocoding**: Convert city names to coordinates: `https://geocoding-api.open-meteo.com/v1/search?name={city}`
- **Weather**: Fetch current conditions: `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=...`
- **Returns**: JSON response with temperature, humidity, wind speed, UV index, weather code
- **Integration**: [WeatherService.java](src/main/java/org/group45/choreday/services/WeatherService.java) handles geocoding + weather fetch in two API calls
- **Error Handling**: Service returns `null` if API fails; controller must check null before using
- **Documentation**: https://open-meteo.com/en/docs

### JSON Parsing
- **Library**: `org.json` (version 20210307)
- **Usage**: Parse API responses in `WeatherService.getWeatherData()`
- **Example**: `JSONObject geoJson = new JSONObject(responseString); JSONArray results = geoJson.getJSONArray("results");`

### PostgreSQL JDBC
- **Driver**: `mysql-connector-java` version 8.0.33
- **Version**: MySQL 5.7+ recommended
- **Connection Pool**: Apache Commons DBCP2 (v2.11.0) for connection pooling
- **Setup**: Ensure database `choreday` exists with tables created before running application

### JavaFX
- **Version**: JavaFX 17.0.14 (matches OpenJDK 17)
- **Components**: javafx-controls, javafx-fxml
- **Entry Point**: [ChoreDayApplication.java](src/main/java/org/group45/choreday/ChoreDayApplication.java) extends `Application`
- **Launcher**: [Launcher.java](src/main/java/org/group45/choreday/Launcher.java) calls `Application.launch()`

### Lombok
- **Version**: 1.18.32
- **Scope**: Provided (compile-time only, not in JAR)
- **Annotation Processor**: Configured in maven-compiler-plugin for code generation

## Project Structure for File Navigation

```
choreday/
├── pom.xml                                 # Maven configuration
├── README.md                               # Basic project description
├── AGENTS.md                               # This AI agent guide
├── src/main/java/org/group45/choreday/
│   ├── Launcher.java                      # Entry point: main() calls Application.launch()
│   ├── ChoreDayApplication.java           # JavaFX Application: loads SignIn.fxml at startup
│   ├── module-info.java                   # Java 17 module declaration
│   ├── controllers/                       # MVC Controller layer (3 classes)
│   │   ├── LoginController.java
│   │   ├── SignUpController.java
│   │   └── DashboardController.java
│   ├── models/                            # Data entities with Lombok (4 classes)
│   │   ├── UserModel.java
│   │   ├── ChoreModel.java
│   │   ├── WeatherRecord.java
│   │   └── WeatherResponse.java
│   ├── services/                          # Business logic & API layer (4 classes)
│   │   ├── SignInService.java
│   │   ├── SignUpService.java
│   │   ├── ChoreService.java
│   │   └── WeatherService.java
│   └── utils/                             # Singletons & helpers (3 classes)
│       ├── DatabaseConnectionPool.java    # Apache Commons DBCP2
│       ├── SessionManager.java            # In-memory user session
│       └── Navigator.java                 # Scene transition utility
├── src/main/resources/org/group45/choreday/
│   ├── SignIn.fxml                        # Login screen (800x600)
│   ├── SignUp.fxml                        # Registration screen
│   ├── Dashboard.fxml                     # Main application screen
│   ├── style.css                          # JavaFX CSS styling
│   └── imgs/                              # Image assets (icons, logos)
├── target/                                # Compiled artifacts (ignore)
└── .git/                                  # Version control

---

**Last Updated**: April 2026 (Latest: April 17, 2026) | **Status**: Actively maintained | For questions, consult individual file JavaDocs or extend this guide with new patterns discovered during development.
