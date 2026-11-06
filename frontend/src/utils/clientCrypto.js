/**
 * clientCrypto.js
 * All cryptography runs in the browser via Web Crypto API.
 *
 * Key flows:
 *  REGISTRATION: generate RSA pair → encrypt private key with PBKDF2+AES → send blob to server
 *  LOGIN:        receive blob from server → PBKDF2+AES decrypt → RSA private key in memory
 *  UPLOAD:       generate AES-256-GCM key → encrypt file → wrap AES key with RSA public key
 *  DOWNLOAD:     unwrap AES key with RSA private key → decrypt file → verify SHA-256
 */

// ─── Base64 helpers ─────────────────────────────────────────────────────────

export function bytesToBase64(bytes) {
  let bin = '';
  const arr = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  arr.forEach(b => (bin += String.fromCharCode(b)));
  return btoa(bin);
}

export function base64ToBytes(b64) {
  // Handle both standard (+/) and URL-safe (-_) Base64
  const std = b64.replace(/-/g, '+').replace(/_/g, '/');
  const padded = std + '=='.slice(0, (4 - (std.length % 4)) % 4);
  const bin = atob(padded);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

// ─── SHA-256 integrity ───────────────────────────────────────────────────────

export async function sha256Hash(data) {
  const buf = data instanceof ArrayBuffer ? data : data.buffer ?? new Uint8Array(data).buffer;
  const hash = await crypto.subtle.digest('SHA-256', buf);
  return bytesToBase64(new Uint8Array(hash));
}

export async function verifyIntegrity(data, expectedB64) {
  return (await sha256Hash(data)) === expectedB64;
}

// ─── PBKDF2 — derive AES-256 key from password ──────────────────────────────

async function deriveKeyFromPassword(password, saltBytes) {
  const pwKey = await crypto.subtle.importKey(
    'raw', new TextEncoder().encode(password), 'PBKDF2', false, ['deriveKey']
  );
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt: saltBytes, iterations: 310000, hash: 'SHA-256' },
    pwKey,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
}

// ─── Encrypt RSA private key with password (PBKDF2 + AES-256-GCM) ───────────

export async function encryptPrivateKeyWithPassword(privateKeyBytes, password) {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const iv   = crypto.getRandomValues(new Uint8Array(12));
  const aesKey = await deriveKeyFromPassword(password, salt);

  const encrypted = await crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    aesKey,
    privateKeyBytes
  );

  return {
    encryptedPrivateKeyBlob: bytesToBase64(new Uint8Array(encrypted)),
    pbkdf2Salt: bytesToBase64(salt),
    pbkdf2Iv:   bytesToBase64(iv),
  };
}

// ─── Decrypt RSA private key using password ──────────────────────────────────

export async function decryptPrivateKeyWithPassword(encryptedBlob, saltB64, ivB64, password) {
  const salt = base64ToBytes(saltB64);
  const iv   = base64ToBytes(ivB64);
  const ciphertext = base64ToBytes(encryptedBlob);
  const aesKey = await deriveKeyFromPassword(password, salt);

  const decrypted = await crypto.subtle.decrypt(
    { name: 'AES-GCM', iv },
    aesKey,
    ciphertext
  );
  return new Uint8Array(decrypted);
}

// ─── RSA key pair generation ─────────────────────────────────────────────────

export async function generateRsaKeyPair() {
  return crypto.subtle.generateKey(
    { name: 'RSA-OAEP', modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: 'SHA-256' },
    true,
    ['encrypt', 'decrypt']
  );
}

export async function exportPublicKey(publicKey) {
  const spki = await crypto.subtle.exportKey('spki', publicKey);
  return bytesToBase64(new Uint8Array(spki));
}

export async function exportPrivateKeyBytes(privateKey) {
  const pkcs8 = await crypto.subtle.exportKey('pkcs8', privateKey);
  return new Uint8Array(pkcs8);
}

export async function importPublicKey(b64) {
  return crypto.subtle.importKey(
    'spki', base64ToBytes(b64),
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    true, ['encrypt']
  );
}

export async function importPrivateKey(bytes) {
  const keyBytes = typeof bytes === 'string' ? base64ToBytes(bytes) : bytes;
  return crypto.subtle.importKey(
    'pkcs8', keyBytes,
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    true, ['decrypt']
  );
}

// ─── AES-256-GCM file encryption ─────────────────────────────────────────────

export async function generateAesKey() {
  return crypto.subtle.generateKey({ name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']);
}

export async function exportAesKey(key) {
  const raw = await crypto.subtle.exportKey('raw', key);
  return new Uint8Array(raw);
}

export async function importAesKey(keyBytes) {
  return crypto.subtle.importKey('raw', keyBytes, { name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']);
}

export async function encryptFile(file, aesKey) {
  const iv  = crypto.getRandomValues(new Uint8Array(12));
  const buf = await file.arrayBuffer();
  const enc = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, aesKey, buf);
  return { encryptedData: new Uint8Array(enc), iv };
}

export async function decryptFile(encryptedBytes, aesKey, iv) {
  const dec = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, aesKey, encryptedBytes);
  return new Uint8Array(dec);
}

// ─── RSA key wrapping ────────────────────────────────────────────────────────

export async function wrapAesKeyWithPublicKey(aesKeyBytes, publicKey) {
  const enc = await crypto.subtle.encrypt({ name: 'RSA-OAEP' }, publicKey, aesKeyBytes);
  return new Uint8Array(enc);
}

export async function unwrapAesKeyWithPrivateKey(encryptedKeyBytes, privateKey) {
  const dec = await crypto.subtle.decrypt({ name: 'RSA-OAEP' }, privateKey, encryptedKeyBytes);
  return new Uint8Array(dec);
}

// ─── Browser download trigger ────────────────────────────────────────────────

export function triggerDownload(bytes, filename, mimeType) {
  const blob = new Blob([bytes], { type: mimeType || 'application/octet-stream' });
  const url  = URL.createObjectURL(blob);
  const a    = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => { document.body.removeChild(a); URL.revokeObjectURL(url); }, 200);
}
