# TalkTranslate 🌐💬
### Real-Time Multilingual Chat Application with Recipient-Centric Translation

TalkTranslate is an intelligent, real-time multilingual chat application backend built with **Spring Boot 3**, **Spring Data JPA (MySQL)**, and **WebSockets (STOMP)**. It automatically detects the sender's language, retrieves the recipient's preferred language, and delivers translated messages instantly over WebSockets.

---

## 🌟 Key Features

- 🎯 **Recipient-Centric Translation**: Senders write in their native language (e.g., Hindi, Hinglish, Spanish); recipients receive and read messages dynamically translated into their preferred language (e.g., English, French, Japanese).
- 🤝 **Social Friendship System (Instagram/FB Style)**: Suggested user discovery, friend requests (`PENDING` ➔ `ACCEPTED` / `REJECTED` / `CANCELLED`), and mutual friend directory.
- ⚡ **Spring Boot STOMP WebSockets**: Real-time bidirectional communication via direct queues (`/user/queue/messages`, `/user/queue/typing`, `/user/queue/errors`).
- 🌐 **Live Preferred Language Switching**: Users can change their target language mid-conversation via REST (`PUT /api/users/language`) or WebSocket (`/app/chat.changeLang`).
- 🧠 **Multi-Tier Translation Engine**: 23+ world languages + Hinglish support with in-memory caching, offline greetings dictionary, and online fallback APIs.
- 🔒 **Secure Authentication**: BCrypt password hashing + lightweight JWT stateless authentication with credentials loaded from environment variables (`.env`).

---

## 📋 Prerequisites

Before running the application, ensure you have:
- **Java JDK 17** or higher (Java 17 to 24 supported)
- **Apache Maven 3.8+**
- **MySQL Server 8.0+** running locally or accessible remotely

---

## ⚙️ Configuration & Setup

### 1. Create MySQL Database
Open your MySQL terminal or MySQL Workbench and run:
```sql
CREATE DATABASE IF NOT EXISTS talktranslate_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Environment (`.env` / `application.properties`)
The application is preconfigured to load credentials from `.env` or [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
# Server Configuration
PORT=8080

# MySQL Database Configuration
DB_URL=jdbc:mysql://localhost:3306/talktranslate_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# JWT Security Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION_MS=86400000
```

---

## 🚀 Running Commands

### 1. Run the Application in Development Mode
To start the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```

*The server will start on [http://localhost:8080](http://localhost:8080).*

---

### 2. Run All Automated Tests
To execute the full unit and integration test suite (16 tests):

```bash
mvn test
```

To run a specific test suite:
```bash
# Run Auth Controller Tests
mvn test -Dtest=AuthControllerIntegrationTest

# Run Chat Service Tests
mvn test -Dtest=ChatServiceTestSuite

# Run Friendship Service Tests
mvn test -Dtest=FriendshipServiceTestSuite
```

---

### 3. Build & Package Standalone JAR
To compile and package the application into an executable JAR file:

```bash
# Clean and package (with tests)
mvn clean package

# Clean and package (skip tests for quick builds)
mvn clean package -DskipTests
```

The compiled JAR will be located at:
```
target/talktranslate-1.0.0.jar
```

---

### 4. Run the Packaged JAR
To run the standalone production JAR:

```bash
java -jar target/talktranslate-1.0.0.jar
```

To run on a custom port or override database credentials at launch:
```bash
java -Dserver.port=9090 -jar target/talktranslate-1.0.0.jar
```

---

## 🧪 Testing REST Endpoints (cURL Examples)

### 1. Application Health & Actuator Metrics
```bash
# Custom Health Check
curl http://localhost:8080/api/health

# Spring Boot Actuator Health Status
curl http://localhost:8080/actuator/health

# Application Metrics Overview
curl http://localhost:8080/actuator/metrics

# JVM Memory Metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### 2. User Sign Up
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rahul_sharma",
    "email": "rahul@test.com",
    "password": "Password123!",
    "fullName": "Rahul Sharma"
  }'
```

### 3. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rahul_sharma",
    "password": "Password123!"
  }'
```

### 4. On-Demand Translation
```bash
curl -X POST http://localhost:8080/api/translate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Hello, how are you?",
    "sourceLang": "en",
    "targetLang": "hi"
  }'
```

### 5. Get Supported Languages
```bash
curl http://localhost:8080/api/languages
```

---

## 📡 WebSocket Endpoints & Topics

| Type | Destination | Description |
|---|---|---|
| **STOMP Handshake Endpoint** | `http://localhost:8080/ws` | SockJS WebSocket Handshake |
| **Inbound Message** | `/app/chat.send` | Send 1-on-1 translated message to friend |
| **Inbound Typing** | `/app/chat.typing` | Broadcast typing indicator |
| **Inbound Lang Change** | `/app/chat.changeLang` | Live language preference update |
| **Outbound User Queue** | `/user/queue/messages` | Recipient-tailored translated message queue |
| **Outbound Typing Queue**| `/user/queue/typing` | Real-time typing status |
| **Outbound Error Queue** | `/user/queue/errors` | Chat error notifications |

---

## 🌐 Complete REST API Reference

### Authentication (`/api/auth`)
- `POST /api/auth/signup` - Register user (`username`, `email`, `password`, `fullName`)
- `POST /api/auth/login` - Authenticate & obtain JWT token
- `GET /api/auth/me` - Get current authenticated user profile (`Authorization: Bearer <token>`)

### Social & Friends (`/api/friends` & `/api/users`)
- `GET /api/users/suggested?userId={id}` - Suggested users discovery
- `POST /api/friends/request/{targetUserId}?userId={myId}` - Send friend request
- `POST /api/friends/accept/{requestId}?userId={myId}` - Accept friend request
- `POST /api/friends/reject/{requestId}?userId={myId}` - Reject friend request
- `DELETE /api/friends/cancel/{requestId}?userId={myId}` - Cancel sent friend request
- `GET /api/friends/requests/incoming?userId={id}` - List incoming pending requests
- `GET /api/friends/requests/outgoing?userId={id}` - List outgoing pending requests
- `GET /api/friends?userId={id}` - List mutual accepted friends

### Chat & Translation (`/api/chat` & `/api`)
- `GET /api/chat/history/{friendId}?userId={myId}` - Chat history between mutual friends
- `PUT /api/users/language` - Update user preferred language `{ "userId": "...", "preferredLanguage": "es" }`
- `GET /api/languages` - List 23+ supported languages (including Hinglish)
- `POST /api/translate` - On-demand translation `{ "text": "...", "sourceLang": "en", "targetLang": "hi" }`
- `GET /api/health` - Server health status

---

## 📁 Project Structure

```
TalkTranslate/
├── pom.xml                               # Pure Java Maven Build Configuration
├── PLAN.md                               # Architecture & Design Plan
├── .env                                  # Local Environment Variables
├── .env.example                          # Environment Variables Template
├── README.md                             # Documentation & Setup Guide
└── src/
    ├── main/
    │   ├── java/com/talktranslate/
    │   │   ├── TalkTranslateApplication.java
    │   │   ├── config/                   # WebSocket, CORS, Security & Dotenv Config
    │   │   ├── controller/               # Auth, User, Friendship, Chat, & API Controllers
    │   │   ├── exception/                # GlobalExceptionHandler & Custom Exceptions
    │   │   ├── model/                    # JPA Entities (User, Friendship, ChatMessage) & DTOs
    │   │   ├── repository/               # Spring Data JPA Repositories
    │   │   └── service/                  # Auth, Friendship, User, Chat, & Translation Services
    │   └── resources/
    │       ├── application.properties    # Dynamic Environment Configuration
    │       └── META-INF/                 # EnvironmentPostProcessor Auto-Configuration
    └── test/                             # 16 Comprehensive Automated Test Suites
```
