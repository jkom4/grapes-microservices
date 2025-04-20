# Payment Backend Service

This project simulates a payment backend implementing a simplified flow inspired by 3D Secure with mutual SSL/TLS communication between components (Client Backend, ACS, ACQ).

## Prerequisites

* **Java JDK** (Version 21 or higher, including `keytool`)
* **MariaDB/MySQL**
* **Git**
* **Twilio** (For actual SMS OTP sending )

## Initial Configuration

### Database

The system requires a MariaDB database named `payment_db`. Check the connection settings in `src/main/resources/application.properties` if you need to modify credentials (default user: `root` with password `SparringMASI!`).

## SSL/TLS Configuration (Keystores & Truststores)

This project uses mTLS (mutual TLS) between its internal components. The following commands generate the necessary keystores and truststores with self-signed certificates for a local test environment.

**Password used for all keystores/keys:** `bankservicepassword`

### Certificate Generation Steps

From the root directory of the `payment-backend` project:

1. **Create directory (if not exists)**
   ```bash
   mkdir -p src/main/resources/keystore
   ```

2. **Generate Identity Keystores (Private key + Self-signed public certificate)**

   a) Client/Backend Keystore (`client_keystore.jks`)
   ```bash
   keytool -genkeypair -alias client -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -storetype PKCS12 -keystore src/main/resources/keystore/client_keystore.jks -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=payment.client.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
   ```

   b) ACS Keystore (`acs_keystore.jks`)
   ```bash
   keytool -genkeypair -alias acs -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -storetype PKCS12 -keystore src/main/resources/keystore/acs_keystore.jks -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=acs.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
   ```

   c) ACQ Keystore (`acq_keystore.jks`)
   ```bash
   keytool -genkeypair -alias acq -keyalg RSA -keysize 3072 -sigalg SHA384withRSA -storetype PKCS12 -keystore src/main/resources/keystore/acq_keystore.jks -storepass bankservicepassword -keypass bankservicepassword -validity 365 -dname "CN=acq.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"
   ```

3. **Export Public Certificates from Keystores**

   a) Export Client Public Certificate
   ```bash
   keytool -exportcert -keystore src/main/resources/keystore/client_keystore.jks -storetype PKCS12 -alias client -file client.cer -storepass bankservicepassword
   ```

   b) Export ACS Public Certificate
   ```bash
   keytool -exportcert -keystore src/main/resources/keystore/acs_keystore.jks -storetype PKCS12 -alias acs -file acs.cer -storepass bankservicepassword
   ```

   c) Export ACQ Public Certificate
   ```bash
   keytool -exportcert -keystore src/main/resources/keystore/acq_keystore.jks -storetype PKCS12 -alias acq -file acq.cer -storepass bankservicepassword
   ```

4. **Create Truststores and Import Required Public Certificates**

   a) Client/Backend Truststore (`client_truststore.jks`): Must trust ACS and ACQ
   ```bash
   keytool -importcert -keystore src/main/resources/keystore/client_truststore.jks -storetype JKS -alias acs_trusted -file acs.cer -storepass bankservicepassword -noprompt
   keytool -importcert -keystore src/main/resources/keystore/client_truststore.jks -storetype JKS -alias acq_trusted -file acq.cer -storepass bankservicepassword -noprompt
   ```

   b) ACS Truststore (`acs_truststore.jks`): Must trust Client and ACQ
   ```bash
   keytool -importcert -keystore src/main/resources/keystore/acs_truststore.jks -storetype JKS -alias client_trusted -file client.cer -storepass bankservicepassword -noprompt
   keytool -importcert -keystore src/main/resources/keystore/acs_truststore.jks -storetype JKS -alias acq_trusted -file acq.cer -storepass bankservicepassword -noprompt
   ```

   c) ACQ Truststore (`acq_truststore.jks`): Must trust ACS
   ```bash
   keytool -importcert -keystore src/main/resources/keystore/acq_truststore.jks -storetype JKS -alias acs_trusted -file acs.cer -storepass bankservicepassword -noprompt
   ```

5. **(Optional) Clean up temporary certificate files**
   ```bash
   rm client.cer acs.cer acq.cer
   ```

### Verification (Optional)

List the content of each file to verify:

```bash
keytool -list -v -keystore src/main/resources/keystore/client_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acs_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acq_keystore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/client_truststore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acs_truststore.jks -storepass bankservicepassword
keytool -list -v -keystore src/main/resources/keystore/acq_truststore.jks -storepass bankservicepassword
```

## Auth Service

The authentication service component uses:

* **Backend**: Spring Boot, Spring Security, MongoDB
* **Frontend**: React, Axios

