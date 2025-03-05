
# Auth-Service

This project implements an **authentication service** with **Spring Boot** for the backend and **React** for the frontend.

## Backend (Spring Boot)

- The backend is a **REST API** exposing endpoints for authentication (login, registration).
- **Default port**: `8091`
- You can access the **Swagger** interface to test the API at the following address:  
  [http://127.0.0.1:8091/swagger-ui/index.html](http://127.0.0.1:8091/swagger-ui/index.html)

### Running the Backend

1. Clone the project and navigate to the **backend** folder:

    ```bash
    git clone <your-repository-url>
    cd auth-service/backend
    ```

2. Install dependencies and run the project:

    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

### Main Endpoints

- **POST /api/auth/login**: Authenticate user (generates a JWT)
- **POST /api/auth/register**: Register a new user

## Frontend (React)

- The frontend is a **React** application that allows users to log in and register via the backend API.

### Running the Frontend

1. Navigate to the **frontend** folder:

    ```bash
    cd auth-service/auth-frontend
    ```

2. Install dependencies and run the project:
    ```bash
    npm install
    npm start
    ```

   The React application will be available at [http://localhost:3000](http://localhost:3000).
## Technologies Used

- **Backend**: Spring Boot, Spring Security, MongoDB, Swagger
- **Frontend**: React, Axios

---
