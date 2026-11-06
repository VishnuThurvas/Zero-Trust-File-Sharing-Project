import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { decryptPrivateKeyWithPassword } from '../utils/clientCrypto';

const S = {
  page:   { minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center',
            background:'linear-gradient(135deg,#070d1a 0%,#0d1b2a 100%)' },
  card:   { background:'#0d1b2a', border:'1px solid #1e3a5f', borderRadius:16, padding:40,
            width:'100%', maxWidth:420, boxShadow:'0 25px 60px rgba(0,0,0,.6)' },
  logo:   { textAlign:'center', marginBottom:28 },
  logotx: { fontSize:28, fontWeight:700, color:'#38bdf8', fontFamily:'monospace' },
  logosb: { fontSize:12, color:'#64748b', marginTop:4 },
  badge:  { display:'flex', alignItems:'center', gap:10, background:'rgba(56,189,248,.05)',
            border:'1px solid rgba(56,189,248,.15)', borderRadius:8, padding:'8px 12px', marginBottom:20 },
  btext:  { fontSize:12, color:'#94a3b8' },
  label:  { display:'block', fontSize:11, fontWeight:700, color:'#94a3b8', marginBottom:5,
            textTransform:'uppercase', letterSpacing:'.06em' },
  input:  { width:'100%', padding:'11px 14px', background:'#070d1a', border:'1px solid #1e3a5f',
            borderRadius:8, color:'#e2e8f0', fontSize:14, outline:'none', marginBottom:16 },
  btn:    { width:'100%', padding:13, background:'linear-gradient(135deg,#0ea5e9,#38bdf8)',
            border:'none', borderRadius:8, color:'#fff', fontSize:15, fontWeight:700, marginTop:6 },
  err:    { background:'rgba(239,68,68,.1)', border:'1px solid rgba(239,68,68,.3)',
            borderRadius:8, padding:'10px 14px', color:'#f87171', fontSize:13, marginBottom:14 },
  link:   { textAlign:'center', marginTop:20, fontSize:13, color:'#64748b' },
};

export default function LoginPage() {
  const [form, setForm]       = useState({ username:'', password:'' });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep]       = useState('');
  const { login, savePrivateKey } = useAuth();
  const navigate = useNavigate();

  const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async e => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      // Step 1 — authenticate with server, receive JWT + encrypted key blob
      setStep('Authenticating…');
      const res = await authApi.login(form);
      const d   = res.data;

      // Step 2 — use password to PBKDF2-decrypt the private key blob (in browser)
      // Server sent: encryptedPrivateKeyBlob + pbkdf2Salt + pbkdf2Iv
      setStep('Decrypting your private key…');
      let privateKeyBytes = null;
      if (d.encryptedPrivateKeyBlob && d.pbkdf2Salt && d.pbkdf2Iv) {
        try {
          privateKeyBytes = await decryptPrivateKeyWithPassword(
            d.encryptedPrivateKeyBlob, d.pbkdf2Salt, d.pbkdf2Iv, form.password
          );
        } catch {
          // Wrong password or corrupted blob — login still works, but decryption won't
          setError('Warning: Could not decrypt private key. Files will not be downloadable.');
        }
      }

      // Step 3 — store in context
      login({ username: d.username, role: d.role, email: d.email, publicKey: d.publicKey },
             d.token, d.refreshToken);

      if (privateKeyBytes) savePrivateKey(privateKeyBytes);

      setStep('');
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
      setStep('');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.logo}>
          <div style={S.logotx}>🔐 SecureShare</div>
          <div style={S.logosb}>Zero-Trust End-to-End Encrypted File Sharing</div>
        </div>

        <div style={S.badge}>
          <span style={{fontSize:18}}>🛡️</span>
          <span style={S.btext}>AES-256-GCM · RSA-2048 · PBKDF2 · Zero-Knowledge</span>
        </div>

        {error && <div style={S.err}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={S.label}>Username</label>
          <input style={S.input} placeholder="Your username"
            value={form.username} onChange={set('username')} required />

          <label style={S.label}>Password</label>
          <input style={S.input} type="password" placeholder="Your password"
            value={form.password} onChange={set('password')} required />

          <button style={S.btn} disabled={loading}>
            {loading ? step || 'Logging in…' : 'Login Securely'}
          </button>
        </form>

        <div style={S.link}>
          No account? <Link to="/register">Register</Link>
        </div>
      </div>
    </div>
  );
}
