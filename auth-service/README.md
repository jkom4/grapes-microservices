
# Auth-Service

This project implements an **authentication service** with **Spring Boot** for the backend and **React** for the frontend.

## Backend (Spring Boot)

- The backend is a **REST API** exposing endpoints for authentication (login, registration).
- **Default port**: `8091`
- You can access the **Swagger** interface to test the API at the following address:  
  [http://127.0.0.1:8091/swagger-ui/index.html](http://127.0.0.1:8091/swagger-ui/index.html)

### Run mongo sh command
```bash
docker exec -it grapes-mongodb mongosh "mongodb://root:SparringMASI%21@localhost:27017/admin"
```

The main DB is `db_auth` and the main collection is `users`.

To use them is necessary to create a user with the following command:
```bash
use db_auth
db.find()
```

### Running the Backend

1. Clone the project and navigate to the **backend** folder:

    ```bash
    git clone <your-repository-url>
    cd auth-service/backend
    ```

2. Export your environment variables:

   ```bash
   nano ~/.zshrc
   ```
   And add the following lines at the end:
    ```bash
    export GRAPES_TWILIO_API_SECRET="your-twilio-api-key" 
    export GRAPES_AES_KEY="your-aes-key"
    export GRAPES_JWT_SECRET_KEY="your-jwt-secret-key"
    ```

   Get your twilio/sendgrip api key [here](https://app.sendgrid.com/settings/api_keys)
   Get your AES and JWT keys in the console at the first start of the service

   ```bash
    source ~/.zshrc
    ```

    *Note that the `GRAPES_AES_KEY` must be a 16-character string.* (you can find a generated key that you can paste in the console when you start the service)

3. Run the following command to start the **MongoDB** and **Spring Boot** services:

    ```bash
    docker-compose up -d mongodb auth-service
    ```
4. Install dependencies and run the project:

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
