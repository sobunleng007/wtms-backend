# WTMS Backend (Spring Boot) By SO BUNLENG

## Overview
This project is a specialized Internal Training Management System backend built with Spring Boot. It provides RESTful APIs designed specifically to support Wing’s employee training programs by managing structured learning materials, session scheduling, and performance evaluations. The backend is architected for security and seamless integration with the WTMS frontend.

## Features
- Training & Session Management
- Centralized Material Repository
- Assignment & Evaluation
- Submission Tracking
- Attendance Monitoring
- Automated Notifications
- Profile & Department Management
- Role-Based Security (RBAC)
- Performance Analytics
- Responsive Integration

## Technology Stack
- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- Maven
- Docker (optional)
- Cloudinary (1.39.0)
- JSON Web Token (jjwt 0.11.5)
- pringDoc OpenAPI (2.8.5)
- Spring Boot Actuator
- H2/MySQL/PostgreSQL (configurable)

## Project Structure
- `src/main/java/com/wtmsbackend/` — Main application code
- `src/main/resources/` — Configuration files
- `src/test/java/` — Test cases
- `pom.xml` — Maven build configuration
- `Dockerfile`, `docker-compose.yaml` — Containerization support

## Setup Instructions
### Prerequisites
- Java JDK 21 or higher
- Maven (or use the included `mvnw.cmd` wrapper)
- Git
- (Optional) Docker & Docker Compose

### Clone the Repository
```
git clone <your-repo-url>
cd backend
```

### Configure the Application
Edit `src/main/resources/application.properties` or `application-dev.yaml` to set up your database and environment variables as needed.

### Build the Project
```
./mvnw.cmd clean install
```

### Run the Application
```
./mvnw.cmd spring-boot:run
```
The backend will start on [http://localhost:8080](http://localhost:8080).

## API Usage Overview
### Training Session Endpoints
- **Create Session**
    - `POST /api/v1/sessions`
    - Request body: `{ "title": "Session Title", "description": "Session Description", "scheduledDate": "2024-07-01T10:00:00" }`
- **Get All Sessions**
    - `GET /api/v1/sessions`
- **Assign Material**
    - `GET /api/v1/materials`
    - Request body: `{ "departmentId": 1 }` 

> For other modules (material, department, assignment, notification, Submission  etc.), similar RESTful endpoints are available under `/api/v1/`.

## Running Tests
```
./mvnw.cmd test
```
Test results will be available in the `target/surefire-reports/` directory.

## Docker Usage
### Build Docker Image
```
docker build -t wtms-backend .
```
### Run with Docker Compose
```
docker-compose up --build
```
This will start the backend and any dependent services as defined in `docker-compose.yaml`.