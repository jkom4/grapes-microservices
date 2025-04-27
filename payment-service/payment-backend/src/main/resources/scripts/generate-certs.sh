#!/bin/bash
set -e

# === Configuration ===
KEYSTORE_DIR=src/main/resources/keystore
set /p KEYSTORE_PASSWORD=Entrer votre mot de passe :

echo "🔧 Création du dossier $KEYSTORE_DIR si nécessaire..."
mkdir -p "$KEYSTORE_DIR"

echo "🔐 Génération des keystores..."

# Client Keystore
keytool -genkeypair -alias client -keyalg RSA -keysize 3072 -sigalg SHA384withRSA \
 -storetype PKCS12 -keystore "$KEYSTORE_DIR/client_keystore.jks" \
 -storepass $PASSWORD -keypass $PASSWORD -validity 365 \
 -dname "CN=payment.client.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"

# ACS Keystore
keytool -genkeypair -alias acs -keyalg RSA -keysize 3072 -sigalg SHA384withRSA \
 -storetype PKCS12 -keystore "$KEYSTORE_DIR/acs_keystore.jks" \
 -storepass $PASSWORD -keypass $PASSWORD -validity 365 \
 -dname "CN=acs.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"

# ACQ Keystore
keytool -genkeypair -alias acq -keyalg RSA -keysize 3072 -sigalg SHA384withRSA \
 -storetype PKCS12 -keystore "$KEYSTORE_DIR/acq_keystore.jks" \
 -storepass $PASSWORD -keypass $PASSWORD -validity 365 \
 -dname "CN=acq.server.local, OU=Payment, O=Grapes, L=Liege, ST=Liege, C=BE"

echo "📤 Export des certificats publics..."

keytool -exportcert -keystore "$KEYSTORE_DIR/client_keystore.jks" -storetype PKCS12 \
 -alias client -file client.cer -storepass $PASSWORD

keytool -exportcert -keystore "$KEYSTORE_DIR/acs_keystore.jks" -storetype PKCS12 \
 -alias acs -file acs.cer -storepass $PASSWORD

keytool -exportcert -keystore "$KEYSTORE_DIR/acq_keystore.jks" -storetype PKCS12 \
 -alias acq -file acq.cer -storepass $PASSWORD

echo "📥 Création des truststores..."

# Client Truststore
keytool -importcert -keystore "$KEYSTORE_DIR/client_truststore.jks" -storetype JKS \
 -alias acs_trusted -file acs.cer -storepass $PASSWORD -noprompt
keytool -importcert -keystore "$KEYSTORE_DIR/client_truststore.jks" -storetype JKS \
 -alias acq_trusted -file acq.cer -storepass $PASSWORD -noprompt

# ACS Truststore
keytool -importcert -keystore "$KEYSTORE_DIR/acs_truststore.jks" -storetype JKS \
 -alias client_trusted -file client.cer -storepass $PASSWORD -noprompt
keytool -importcert -keystore "$KEYSTORE_DIR/acs_truststore.jks" -storetype JKS \
 -alias acq_trusted -file acq.cer -storepass $PASSWORD -noprompt

# ACQ Truststore
keytool -importcert -keystore "$KEYSTORE_DIR/acq_truststore.jks" -storetype JKS \
 -alias acs_trusted -file acs.cer -storepass $PASSWORD -noprompt

echo "🧹 Suppression des fichiers de certificats temporaires..."
rm -f client.cer acs.cer acq.cer

echo "✅ Script terminé avec succès."
