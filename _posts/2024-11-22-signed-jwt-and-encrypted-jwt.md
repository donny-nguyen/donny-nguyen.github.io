# Signed JWTs and Encrypted JWTs

The key difference between **signed JWTs** and **encrypted JWTs** lies in their purpose and how they secure the data they carry. Here's a detailed breakdown:

---

### **Signed JWTs**
1. **Purpose**:
   - To ensure the authenticity and integrity of the token. 
   - Allows the recipient to verify that the token was issued by a trusted source and has not been tampered with.
   
2. **How it Works**:
   - The JWT is **signed** using a secret key (HMAC) or a private key (RSA or ECDSA).
   - The recipient uses the corresponding secret or public key to verify the signature.
   - The data in the payload is **not encrypted** and is readable by anyone who has the token.

3. **Use Case**:
   - Commonly used for **authentication** in APIs or web applications.
   - Example: Ensuring that a user’s identity is valid without storing session data on the server.

4. **Security**:
   - Protects against tampering but **does not protect the confidentiality** of the data. Sensitive information in the payload should not be included without additional encryption.

5. **Example**:
   ```json
   {
     "header": {
       "alg": "HS256",
       "typ": "JWT"
     },
     "payload": {
       "sub": "1234567890",
       "name": "John Doe",
       "admin": true
     },
     "signature": "base64Url(header).base64Url(payload).HMACSHA256(secret)"
   }
   ```

---

### **Encrypted JWTs**
1. **Purpose**:
   - To ensure the **confidentiality** of the data.
   - The contents of the token are encrypted so only the intended recipient can read them.

2. **How it Works**:
   - The JWT is **encrypted** using a public key (in asymmetric encryption) or a shared secret key (in symmetric encryption).
   - Only the holder of the private key (in asymmetric encryption) or the shared secret (in symmetric encryption) can decrypt the token to access the payload.

3. **Use Case**:
   - Used when sensitive data (e.g., Personally Identifiable Information, financial data) must be securely transmitted.
   - Example: A secure exchange of sensitive information between two systems.

4. **Security**:
   - Protects both the **confidentiality** and the integrity of the data.
   - Even if the token is intercepted, its payload cannot be read without the decryption key.

5. **Example**:
   ```json
   {
     "header": {
       "alg": "RSA-OAEP",
       "enc": "A256GCM"
     },
     "encrypted_payload": "Encrypted data"
   }
   ```

---

### **Key Differences**

| **Aspect**          | **Signed JWT**                             | **Encrypted JWT**                        |
|----------------------|--------------------------------------------|------------------------------------------|
| **Purpose**          | Authenticity and integrity                | Confidentiality and integrity            |
| **Data Visibility**  | Payload is visible to anyone              | Payload is hidden (encrypted)            |
| **Use Case**         | Authentication or integrity verification  | Secure transmission of sensitive data    |
| **Security**         | Protects against tampering only           | Protects against tampering and snooping  |
| **Example Algorithm**| HMAC (HS256), RSA (RS256)                 | RSA-OAEP, AES                            |

---

### **Combined Use**
It’s possible to both **sign** and **encrypt** a JWT for maximum security:
1. First, **sign the JWT** to ensure its authenticity and integrity.
2. Then, **encrypt the signed JWT** to ensure confidentiality.

This approach is often used when both the sender and recipient need to verify and protect sensitive data securely.