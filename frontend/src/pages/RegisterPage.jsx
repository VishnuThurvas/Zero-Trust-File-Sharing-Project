import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  generateRsaKeyPair, exportPublicKey, exportPrivateKeyBytes,
  encryptPrivateKeyWithPassword, importPrivateKey
} from '../utils/clientCrypto';

const S = {
  page:  { minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center',
           background:'linear-gradient(135deg,#070d1a 0%,#0d1b2a 100%)', padding:20 },
  card:  { background:'#0d1b2a', border:'1px solid #1e3a5f', borderRadius:16, padding:40,
           width:'100%', maxWidth:460, boxShadow:'0 25px 60px rgba(0,0,0,.6)' },
  title: { fontSize:26, fontWeight:700, color:'#38bdf8', fontFamily:'monospace', marginBottom:4 },
  sub:   { fontSize:13, color:'#64748b', marginBottom:24, lineHeight:1.5 },
  label: { display:'block', fontSize:11, fontWeight:700, color:'#94a3b8', marginBottom:5,
           textTransform:'uppercase', letterSpacing:'.06em' },
  input: { width:'100%', padding:'11px 14px', background:'#070d1a', border:'1px solid #1e3a5f',
           borderRadius:8, color:'#e2e8f0', fontSize:14, outline:'none', marginBottom:16,
           transition:'border-color .2s' },
  btn:   { width:'100%', padding:13, background:'linear-gradient(135deg,#0ea5e9,#38bdf8)',
           border:'none', borderRadius:8, color:'#fff', fontSize:15, fontWeight:700, marginTop:6 },
  err:   { background:'rgba(239,68,68,.1)', border:'1px solid rgba(239,68,68,.3)',
           borderRadius:8, padding:'10px 14px', color:'#f87171', fontSize:13, marginBottom:14 },
  info:  { background:'rgba(56,189,248,.06)', border:'1px solid rgba(56,189,248,.2)',
           borderRadius:8, padding:'12px 14px', fontSize:12, color:'#94a3b8', marginBottom:18, lineHeight:1.7 },
  step:  { display:'flex', alignItems:'flex-start', gap:10, marginBottom:10 },
  dot:   { width:22, height:22, borderRadius:'50%', background:'rgba(56,189,248,.15)',
           border:'1px solid #38bdf8', color:'#38bdf8', fontSize:11, fontWeight:700,
           display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, marginTop:1 },
  link:  { textAlign:'center', marginTop:20, fontSize:13, color:'#64748b' },
};

export default function RegisterPage() {
  const [form, setForm]       = useState({ username:'', email:'', password:'', confirm:'' });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep]       = useState('idle'); // idle | generating | encrypting | saving | done
  const { login, savePrivateKey } = useAuth();
  const navigate = useNavigate();

  const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async e => {
    e.preventDefault();
    if (form.password !== form.confirm) { setError('Passwords do not match'); return; }
    if (form.password.length < 8) { setError('Password must be at least 8 characters'); return; }
    setError(''); setLoading(true);

    try {
      // Step 1 — generate RSA-2048 key pair in browser
      setStep('generating');
      const keyPair = await generateRsaKeyPair();
      const publicKeyB64     = await exportPublicKey(keyPair.publicKey);
      const privateKeyBytes  = await exportPrivateKeyBytes(keyPair.privateKey);

      // Step 2 — encrypt private key with user's password via PBKDF2 + AES-256-GCM
      // Raw private key NEVER sent to server
      setStep('encrypting');
      const { encryptedPrivateKeyBlob, pbkdf2Salt, pbkdf2Iv } =
        await encryptPrivateKeyWithPassword(privateKeyBytes, form.password);

      // Step 3 — send encrypted blob + public key to server
      setStep('saving');
      await authApi.register({
        username: form.username,
        email:    form.email,
        password: form.password,
        publicKey: publicKeyB64,
        encryptedPrivateKeyBlob,
        pbkdf2Salt,
        pbkdf2Iv,
      });

      // Step 4 — auto-login and store private key in sessionStorage
      const loginRes = await authApi.login({ username: form.username, password: form.password });
      const d = loginRes.data;
      login({ username: d.username, role: d.role, email: d.email, publicKey: d.publicKey },
             d.token, d.refreshToken);

      // Keep decrypted private key bytes in sessionStorage for this session
      savePrivateKey(privateKeyBytes);
      setStep('done');
      navigate('/dashboard');

    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed');
      setStep('idle');
    } finally {
      setLoading(false);
    }
  };

  const stepLabel = {
    idle:       'Create Secure Account',
    generating: '🔑 Generating RSA-2048 key pair…',
    encrypting: '🔒 Encrypting private key with PBKDF2+AES…',
    saving:     '📤 Saving encrypted blob to server…',
    done:       '✅ Done!',
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.title}>🔐 Create Account</div>
        <div style={S.sub}>Your private key never leaves your device unencrypted.</div>

        <div style={S.info}>
          {[
            ['1','RSA-2048 key pair generated in your browser'],
            ['2','Private key encrypted with your password (PBKDF2 + AES-256-GCM)'],
            ['3','Only the encrypted blob is stored on server'],
            ['4','Server cannot decrypt your files — ever'],
          ].map(([n,t]) => (
            <div key={n} style={S.step}>
              <div style={S.dot}>{n}</div>
              <span>{t}</span>
            </div>
          ))}
        </div>

        {error && <div style={S.err}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={S.label}>Username</label>
          <input style={S.input} placeholder="Choose a username"
            value={form.username} onChange={set('username')} required />

          <label style={S.label}>Email</label>
          <input style={S.input} type="email" placeholder="your@email.com"
            value={form.email} onChange={set('email')} required />

          <label style={S.label}>Password</label>
          <input style={S.input} type="password" placeholder="Min 8 characters"
            value={form.password} onChange={set('password')} required />

          <label style={S.label}>Confirm Password</label>
          <input style={S.input} type="password" placeholder="Repeat password"
            value={form.confirm} onChange={set('confirm')} required />

          <button style={S.btn} disabled={loading}>
            {stepLabel[step]}
          </button>
        </form>

        <div style={S.link}>Already have an account? <Link to="/login">Login</Link></div>
      </div>
    </div>
  );
}
