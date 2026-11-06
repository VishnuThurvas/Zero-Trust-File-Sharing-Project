# 🔐 Zero-Trust Secure File Sharing Platform

**Final Year Cybersecurity Project — with PBKDF2 Password-Based Key Protection**

---

## 🔑 Key Security Innovation — Password-Protected Private Key

This version replaces the insecure "save your private key as a text file" approach with
**PBKDF2 + AES-256-GCM encrypted key storage**.

### How It Works

```
REGISTRATION:
1. Browser generates RSA-2048 key pair
2. Password  ──PBKDF2(310,000 iterations + salt)──▶  AES-256 key
3. AES-256 key  ──AES-GCM──▶  Encrypted private key blob
4. Server receives: { publicKey, encryptedBlob, salt, iv }
5. Raw private key NEVER sent to server

LOGIN:
1. Server returns: { encryptedBlob, salt, iv }
2. Browser: PBKDF2(password + salt)  ──▶  AES-256 key
3. AES-256 key decrypts blob  ──▶  RSA private key in memory
4. Private key lives only in browser sessionStorage (cleared on tab close)
```

The server cannot decrypt files even with full database access.

---

## 🚀 Quick Start

### Development (no Docker needed)

```bash
# Terminal 1 — Backend (API on :8080)
cd backend
mvn spring-boot:run

# Terminal 2 — Frontend (UI on :3000)
cd frontend
npm install
npm run dev
```

Open http://localhost:3000

### Production (Docker)

```bash
# From project root (where docker-compose.yml is)
docker-compose up -d
```

Open http://localhost

---

## 🛡️ Security Features

| Feature | Technology |
|---------|-----------|
| File encryption | AES-256-GCM (client-side) |
| Key exchange | RSA-2048-OAEP |
| Private key storage | PBKDF2 (310k iter) + AES-256-GCM |
| Integrity verification | SHA-256 hash comparison |
| Authentication | JWT (HMAC-SHA512) |
| Brute force protection | Account lockout after 5 attempts |
| Tamper-proof logs | SHA-256 hash chain |
| Deception security | Honey files with admin alerts |
| Malware detection | ClamAV + signature fallback |
| Self-destructing files | Time/download-count expiry |
| Access control | RBAC (Admin/Owner/Editor/Viewer) |

---

## 🗂️ Project Structure

```
secure-file-sharing/
├── backend/
│   ├── src/main/java/com/secureshare/
│   │   ├── controller/   AuthController, FileController, AuditController
│   │   ├── service/      AuthService, FileService, AuditLogService, MalwareScanService
│   │   ├── entity/       User (with encryptedPrivateKeyBlob), FileRecord, FileShare, AuditLog
│   │   ├── security/     JwtAuthenticationFilter
│   │   └── util/         CryptoUtil, JwtUtil
│   └── pom.xml
├── frontend/
│   └── src/
│       ├── utils/clientCrypto.js   PBKDF2, AES-GCM, RSA, SHA-256 (Web Crypto API)
│       ├── pages/                  RegisterPage, LoginPage, DashboardPage, AuditPage
│       └── context/AuthContext.jsx Private key session management
├── docker-compose.yml
└── README.md
```
