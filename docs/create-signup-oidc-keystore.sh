#!/bin/bash

# Variables
KEYSTORE_PASSWORD="signup-oidc-password"
KEY_ALIAS="mosip-signup-oauth-client"
KEYSTORE_FILE="oidckeystore.p12"
PRIVATE_KEY_FILE="private_key.pem"
PUBLIC_KEY_FILE="public_key.pem"
JWK_FILE="public_key.jwk"
CERT_FILE="cert.pem"

# Step 1: Generate RSA key pair
openssl genpkey -algorithm RSA -out $PRIVATE_KEY_FILE -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in $PRIVATE_KEY_FILE -out $PUBLIC_KEY_FILE

# Step 2: Create a self-signed certificate for the private key
openssl req -new -x509 -key $PRIVATE_KEY_FILE -out $CERT_FILE -days 365 \
  -subj "/C=IN/ST=State/L=City/O=Organization/OU=OrgUnit/CN=example.com"

# Step 3: Generate a PKCS12 keystore containing the private key and certificate
openssl pkcs12 -export -inkey $PRIVATE_KEY_FILE -in $CERT_FILE -out $KEYSTORE_FILE \
  -name $KEY_ALIAS -password pass:$KEYSTORE_PASSWORD

# Step 4: Extract the public key components (modulus 'n' and exponent 'e') using openssl
MODULUS_HEX=$(openssl rsa -pubin -in $PUBLIC_KEY_FILE -modulus -noout | cut -d'=' -f2)
EXPONENT_DEC=$(openssl rsa -pubin -in $PUBLIC_KEY_FILE -text -noout | grep "Exponent:" | awk '{print $2}')

# Function to convert hex to base64url
hex_to_base64url() {
    echo $1 | xxd -r -p | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n'
}

# Function to convert decimal to base64url
dec_to_base64url() {
    printf '%x' $1 | xxd -r -p | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n'
}

# Convert the modulus (n) and exponent (e) from HEX to base64url
MODULUS_BASE64URL=$(hex_to_base64url $MODULUS_HEX)
EXPONENT_BASE64URL=$(dec_to_base64url $EXPONENT_DEC)

# Step 6: Create JWK JSON structure
cat > $JWK_FILE <<EOL
{
  "kty": "RSA",
  "n": "$MODULUS_BASE64URL",
  "e": "$EXPONENT_BASE64URL",
  "alg": "RS256",
  "use": "sig"
}
EOL

rm $PRIVATE_KEY_FILE $PUBLIC_KEY_FILE $CERT_FILE

# Completion messages
echo "PKCS12 keystore generated: $KEYSTORE_FILE"
echo "Public key (JWK) saved to: $JWK_FILE"

