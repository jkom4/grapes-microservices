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

2. Fill the `.env` file at the root to export your environment variables:

   Get your sendgrip api key [here](https://app.sendgrid.com/settings/api_keys)
   Get your twilio auth token and account SID [here](https://console.twilio.com/)
   Get your AES and JWT keys in the console at the first start of the service

   ```bash
    source ~/.zshrc
    ```

    *Note that the `GRAPES_AES_KEY` must be a 16-character string.* (you can find a generated key that you can paste in the console when you start the service)

3. Run the following command to start the **MongoDB** and **Spring Boot** services:

    ```bash
    docker-compose up -d mongodb auth-service
    ```
   
4. For the eID authentication, you need to install the PKCS11 dependencies for maven (which are not on a public repository)

WARNING: first, you need to install OpenSC

On Linux

```bash
sudo apt update
sudo apt install opensc
```

On MacOS

```bash
brew install opensc
```

On Windows


1. Download the latest OpenSC installer from the [OpenSC releases page](https://github.com/OpenSC/OpenSC/releases)
2. Look for a file named like `OpenSC-<version>-win64.msi`
3. Run the installer and follow the installation steps
   Of course! Here's the improved README in English:

---


## Configuring VM Options for IntelliJ IDEA

When running or debugging the application in IntelliJ IDEA, you need to add the following VM options in the configuration settings:

1. Open IntelliJ IDEA and go to the Run/Debug configurations.
2. Select the configuration for your project.
3. In the **Configuration** tab, locate the **VM Options** field.
4. Add the following options:
```
--add-exports=jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED --enable-native-access=ALL-UNNAMED
```
5. Save the changes and restart your application.

These settings enable the application to properly access native APIs and required cryptographic modules. Make sure to include them to avoid compatibility issues.

### Installing Dependencies

Before running the following commands, make sure to specify **the absolute path** of the file in the `-Dfile` option. Additionally, the version must be enclosed in single quotes (`'`).

```bash
cd auth-backend

mvn install:install-file -Dfile="/absolute/path/to/pkcs11-provider-1.9.4.jar" -DgroupId=iaik -DartifactId=pkcs11-provider -Dversion='1.9.4' -Dpackaging=jar

mvn install:install-file -Dfile="/absolute/path/to/sunpkcs11-wrapper-1.4.0.jar" -DgroupId=iaik -DartifactId=pkcs11-provider -Dversion='1.9.4' -Dpackaging=jar

mvn install:install-file -Dfile="/absolute/path/to/iaikPkcs11Provider.jar" -DgroupId=iaik -DartifactId=pkcs11-provider -Dversion='1.9.4' -Dpackaging=jar
```

---

This version ensures clarity regarding the absolute path requirement for `-Dfile` and correctly applies the `'1.9.4'` notation for `-Dversion`. Let me know if you need further refinements! 🚀

5. Install dependencies and run the project:
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
