# 🍕 Awesome Pizza - Order Management System

A RESTful API for pizza order management built with Spring Boot 3.5.4 and Java 21. This system allows customers to place orders without registration and enables pizzeria staff to manage the order queue efficiently.

## 📋 Table of Contents
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Docker Support](#-docker-support)
- [Project Structure](#-project-structure)
- [Design Patterns](#-design-patterns)

## ✨ Features

### Customer Features
- **Place Orders**: Create orders without registration
- **Track Orders**: Check order status using order code
- **No Authentication**: Simple order tracking via unique codes

### Pizzeria Management
- **Queue Management**: View all active orders
- **Order Processing**: Take orders sequentially (FIFO)
- **Status Updates**: Mark orders as ready or completed
- **State Management**: Orders flow through defined states

### Order Lifecycle
```
PENDING → IN_PREPARATION → READY → COMPLETED
```

## 🚀 Tech Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.4** - Framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **JUnit 5 & Mockito** - Testing
- **Maven** - Build tool
- **Docker** - Containerization

## 🏗 Architecture

The application follows a **layered architecture**:

```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database (H2 in-memory)
```

## 🎯 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Docker (optional)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/awesome-pizza.git
cd awesome-pizza
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:pizzadb`
    - Username: `sa`
    - Password: (leave empty)

## 📚 API Documentation

### Interactive Documentation
Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Main Endpoints

#### Customer Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create a new order |
| GET | `/api/v1/orders/{orderCode}/status` | Check order status |

#### Pizzeria Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/pizzeria/queue` | View order queue |
| POST | `/api/v1/pizzeria/orders/next` | Take next order |
| PUT | `/api/v1/pizzeria/orders/{orderCode}/ready` | Mark as ready |
| PUT | `/api/v1/pizzeria/orders/{orderCode}/complete` | Complete order |

#### Menu Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/menu/pizzas` | Get available pizzas |

### Example Requests

#### Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "pizzaId": 1,
        "quantity": 2,
        "notes": "Extra cheese"
      }
    ]
  }'
```

Response:
```json
{
  "id": 1,
  "orderCode": "ABC12345",
  "status": "PENDING",
  "statusDescription": "In attesa",
  "items": [
    {
      "pizzaName": "Margherita",
      "quantity": 2,
      "notes": "Extra cheese"
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Check Order Status
```bash
curl http://localhost:8080/api/v1/orders/ABC12345/status
```

## 🧪 Testing

The project includes comprehensive unit and integration tests.

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=OrderServiceImplTest

# With coverage report
mvn clean test jacoco:report
```

### Test Coverage
- **Service Layer**: Business logic and state transitions
- **Controller Layer**: REST endpoints and validation
- **Repository Layer**: Data persistence
- **Integration Tests**: Complete order flow

Coverage report available at: `target/site/jacoco/index.html`

## 🐳 Docker Support

### Build and Run with Docker

1. **Build the Docker image**
```bash
docker build -t awesome-pizza:latest .
```

2. **Run with Docker Compose**
```bash
docker-compose up -d
```

3. **Access the application**
- Same URLs as above: `http://localhost:8080`

4. **Stop the application**
```bash
docker-compose down
```

### Docker Features
- Multi-stage build for optimized image size (~200MB)
- Non-root user for security
- Health checks included
- Container-optimized JVM settings


## 🎨 Design Patterns

### Implemented Patterns

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic encapsulation
3. **DTO Pattern**: Clean API contracts
4. **Builder Pattern**: Object construction
5. **State Pattern**: Order status transitions with validation
6. **Dependency Injection**: Spring IoC container
7. **RESTful Design**: Resource-based API architecture

### Code Quality

- **SOLID Principles**: Single responsibility, dependency inversion
- **Clean Code**: Meaningful names, small methods, proper abstractions
- **Error Handling**: Global exception handler with proper HTTP status codes
- **Validation**: Input validation using Bean Validation API
- **Testing**: High test coverage with unit and integration tests

## 🔄 Order State Transitions

```
PENDING ──────────► IN_PREPARATION
                           │
                           ▼
                        READY
                           │
                           ▼
                      COMPLETED
```

Valid transitions:
- `PENDING` → `IN_PREPARATION` (when pizzaiolo takes the order)
- `IN_PREPARATION` → `READY` (when pizza is ready)
- `READY` → `COMPLETED` (when delivered to customer)

## 🛠 Configuration

### Application Properties

Default configuration in `application.yml`:
- Server port: 8080
- Database: H2 in-memory
- Hibernate DDL: create-drop
- Swagger UI: enabled

### Initial Data

The application automatically creates sample pizzas on startup:
- Margherita (€8.00)
- Marinara (€7.00)
- Quattro Stagioni (€12.00)
- Diavola (€10.00)

## 📈 Performance Considerations

- **In-memory database**: Fast for development and testing
- **Eager loading**: Optimized for small datasets
- **Connection pooling**: HikariCP for efficient connection management
- **Stateless services**: Horizontally scalable

## 🚦 API Status Codes

- `200 OK`: Successful GET/PUT requests
- `201 Created`: Successful POST requests
- `400 Bad Request`: Validation errors or invalid state transitions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Unexpected errors

## 👥 Author

**Hamid** - [GitHub Profile](https://github.com/HamidCodeHub/ordering-service)

## 📝 License

This project is developed as a technical assessment for Awesome Pizza.

## 🎯 Future Enhancements

Potential improvements for production:
- WebSocket support for real-time order updates
- Authentication and authorization
- PostgreSQL for persistent storage
- Caching with Redis
- Event-driven architecture with Kafka
- Metrics and monitoring with Actuator
- API rate limiting
- Multi-language support

---

**Note**: This is a demonstration project showcasing Spring Boot best practices and clean architecture principles.