
# Payment Backend Service (Simulation 3D Secure)

Ce projet simule un backend de paiement implémentant un flux simplifié inspiré de 3D Secure avec communication SSL/TLS mutuelle entre les composants (Client Backend, ACS, ACQ).

## Prérequis

* **Java JDK** (Version 21 ou supérieure, incluant `keytool`)
* **Maven** (Pour compiler et lancer l'application)
* **MariaDB** (Ou MySQL compatible)
* **Git** (Pour cloner le projet)
* **(Optionnel)** Compte Twilio (Pour l'envoi réel des SMS OTP - non nécessaire si `app.environment=development`)

## Configuration Initiale

### 1. Base de Données

* Assurez-vous que MariaDB est installé et en cours d'exécution.
* Créez une base de données nommée `paymentdb` :
    ```sql
    CREATE DATABASE paymentdb;
    ```
* Vérifiez les informations de connexion dans `src/main/resources/application.properties` et adaptez-les si nécessaire (l'utilisateur par défaut est `root` avec le mot de passe `SparringMASI!`) :
    ```properties
    spring.datasource.url=jdbc:mariadb://localhost:3306/paymentdb
    spring.datasource.username=root
    spring.datasource.password=SparringMASI!
    ```
* Au premier lancement, Hibernate (`spring.jpa.hibernate.ddl-auto=update`) devrait créer les tables nécessaires.

### 2. Twilio (Optionnel)

* Si vous souhaitez tester l'envoi réel de SMS OTP :
    * Créez un compte Twilio.
    * Obtenez votre `Account SID` et `Auth Token`.
    * Obtenez un numéro de téléphone Twilio capable d'envoyer des SMS.
    * Mettez à jour ces informations dans `src/main/resources/application.properties` :
        ```properties
        twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        twilio.auth.token=your_auth_token
        twilio.phone.number=+1234567890 # Votre numéro Twilio expéditeur
        ```
    * Pour désactiver l'envoi réel de SMS (utile en développement), ajoutez ou modifiez cette ligne :
        ```properties
        app.environment=development
        ```
* **Important :** Pour les tests, le numéro de téléphone de l'utilisateur testé dans la base de données **doit être différent** du `twilio.phone.number`.

### 3. Utilisateur de Test

* Assurez-vous d'avoir au moins un utilisateur dans la table `users` de la base `paymentdb`. Vous pouvez l'insérer manuellement. Par exemple :
    ```sql
    -- Le mot de passe 'P@ssw0rd' sera salé et haché par l'application au login
    -- Remplacez +32... par un numéro valide différent de votre numéro Twilio
    INSERT INTO users (login, password, phone_number, account_balance) VALUES ('testuser', '5csK3bw5x6PsQVpIgO4yx0xJTVWc5vzc5mQafQOTeIA=', '+32499999999', 1000.00);
    ```
    *(Note : Le mot de passe '5csK...' est le résultat de `P@ssw0rd` salé avec `HIARD` et haché en SHA-256/Base64 comme défini dans `PasswordManager`)*

## Configuration SSL/TLS (Keystores & Truststores)

Ce projet utilise mTLS (mutual TLS) entre ses composants internes (Client <=> ACS, Client <=> ACQ, ACQ <=> ACS). Les commandes suivantes génèrent les keystores et truststores nécessaires avec des certificats auto-signés pour un environnement de test local.

**Mot de passe utilisé pour tous les keystores/clés :** `bankservicepassword` (modifiable dans les commandes et `application.properties`).

**Ouvrez un terminal dans le répertoire racine du projet `payment-backend`.**

### 1. Créer le Répertoire Keystore

```bash
# Linux/macOS
mkdir -p src/main/resources/keystore

# Windows (si nécessaire)
# mkdir src\main\resources\keystore
```

### 2. Générer les Keystores (Clé Privée + Certificat Public)

* **a) Keystore Client (`client_keystore.jks`) :** Identité du backend principal.
    ```bash
    keytool -genkeypair -alias client -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -keystore src/main/resources/keystore/client_keystore.jks -storetype PKCS12 -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=payment.client.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
    ```

* **b) Keystore ACS (`acs_keystore.jks`) :** Identité du serveur ACS intégré.
    ```bash
    keytool -genkeypair -alias acs -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -keystore src/main/resources/keystore/acs_keystore.jks -storetype PKCS12 -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=acs.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
    ```

* **c) Keystore ACQ (`acq_keystore.jks`) :** Identité du serveur ACQ intégré.
    ```bash
    keytool -genkeypair -alias acq -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -keystore src/main/resources/keystore/acq_keystore.jks -storetype PKCS12 -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=acq.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
    ```

### 3. Exporter les Certificats Publics

* **a) Exporter Certificat Client :**
    ```bash
    keytool -exportcert -keystore src/main/resources/keystore/client_keystore.jks -alias client -file payment_client.cer -storepass bankservicepassword
    ```

* **b) Exporter Certificat ACS :**
    ```bash
    keytool -exportcert -keystore src/main/resources/keystore/acs_keystore.jks -alias acs -file acs_server.cer -storepass bankservicepassword
    ```

* **c) Exporter Certificat ACQ :**
    ```bash
    keytool -exportcert -keystore src/main/resources/keystore/acq_keystore.jks -alias acq -file acq_server.cer -storepass bankservicepassword
    ```

### 4. Créer les Truststores et Importer les Certificats

* **a) Truststore Client (`client_truststore.jks` - pour que ACS fasse confiance au Client) :**
    ```bash
    keytool -importcert -keystore src/main/resources/keystore/client_truststore.jks -alias client_trusted -file payment_client.cer -storepass bankservicepassword -noprompt
    ```

* **b) Truststore ACS (`acs_truststore.jks` - pour que Client et ACQ fassent confiance à ACS) :**
    ```bash
    keytool -importcert -keystore src/main/resources/keystore/acs_truststore.jks -alias acs_server_trusted -file acs_server.cer -storepass bankservicepassword -noprompt
    ```

* **c) Truststore ACQ (`acq_truststore.jks` - pour que Client fasse confiance à ACQ) :**
    ```bash
    keytool -importcert -keystore src/main/resources/keystore/acq_truststore.jks -alias acq_server_trusted -file acq_server.cer -storepass bankservicepassword -noprompt
    ```

* *(Optionnel : supprimez les fichiers `.cer` après import)*
    ```bash
    # Linux/macOS
    # rm *.cer
    # Windows
    # del *.cer
    ```

### 5. Vérification (Optionnelle)

Listez le contenu de chaque fichier pour vérifier :

```bash
keytool -list -v -keystore src/main/resources/keystore/client_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acs_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acq_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/client_truststore.jks -storepass bankservicepassword # Doit contenir client_trusted
keytool -list -v -keystore src/main/resources/keystore/acs_truststore.jks -storepass bankservicepassword  # Doit contenir acs_server_trusted
keytool -list -v -keystore src/main/resources/keystore/acq_truststore.jks -storepass bankservicepassword  # Doit contenir acq_server_trusted
```

## Lancer l'Application

Depuis le répertoire racine du projet :

```bash
mvn spring-boot:run
```

L'application démarrera, incluant les serveurs ACS et ACQ intégrés sur les ports définis dans `application.properties` (8081, 8083, 8082). Le serveur principal écoute sur le port 8043 en HTTP.

## Tester le Flux

1.  **Connexion :** Utilisez un client REST (Postman, Insomnia...) ou le frontend pour envoyer une requête POST à `http://localhost:8043/api/login` avec le `login` et `password` de votre utilisateur de test dans le corps JSON.
2.  **Initiation Paiement :** Envoyez une requête POST à `http://localhost:8043/api/payment/initiate` avec les détails de carte (format `PaymentRequestDTO.java`) dans le corps JSON.
   * Vérifiez les logs du backend.
   * Vérifiez la réception du SMS avec l'OTP sur le téléphone de l'utilisateur de test.
   * La réponse doit contenir `success: true` et un `paymentId`.
3.  **Complétion Paiement :** Envoyez une requête POST à `http://localhost:8043/api/payment/complete` avec l'OTP reçu dans le corps JSON (format `TokenDTO.java`, champ `paymentToken`).
   * Vérifiez les logs : le token doit être envoyé à l'ACQ, puis à l'ACS Money pour vérification.
   * Vérifiez la réponse : elle doit contenir `success: true` et un `transactionId`.

```

Ce fichier README devrait être un bon point de départ pour vos collègues. N'hésitez pas à l'adapter ou à ajouter des détails spécifiques à votre environnement si nécessaire !
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
