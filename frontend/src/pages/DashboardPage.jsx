import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fileApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  generateAesKey, exportAesKey, encryptFile, importPublicKey,
  wrapAesKeyWithPublicKey, bytesToBase64, sha256Hash,
  importPrivateKey, unwrapAesKeyWithPrivateKey, importAesKey,
  decryptFile, base64ToBytes, verifyIntegrity, triggerDownload
} from '../utils/clientCrypto';

// ─── Styles ──────────────────────────────────────────────────────────────────
const C = {
  wrap:    { display:'flex', minHeight:'100vh' },
  side:    { width:230, background:'#0d1b2a', borderRight:'1px solid #1e3a5f',
             display:'flex', flexDirection:'column', minHeight:'100vh' },
  slogo:   { padding:'22px 20px 18px', borderBottom:'1px solid #1e3a5f' },
  slgtx:   { fontSize:18, fontWeight:700, color:'#38bdf8', fontFamily:'monospace' },
  slgsb:   { fontSize:10, color:'#64748b', marginTop:2 },
  nav:     { display:'flex', alignItems:'center', gap:10, padding:'10px 20px',
             color:'#94a3b8', cursor:'pointer', fontSize:14, transition:'all .2s',
             userSelect:'none', border:'none', background:'transparent', width:'100%', textAlign:'left' },
  navA:    { background:'rgba(56,189,248,.08)', color:'#38bdf8', borderLeft:'2px solid #38bdf8' },
  main:    { flex:1, padding:28, overflowY:'auto' },
  hdr:     { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:22 },
  h1:      { fontSize:22, fontWeight:700, color:'#e2e8f0' },
  card:    { background:'#0d1b2a', border:'1px solid #1e3a5f', borderRadius:12, padding:22, marginBottom:16 },
  ctitle:  { fontSize:12, fontWeight:700, color:'#64748b', marginBottom:16,
             textTransform:'uppercase', letterSpacing:'.07em' },
  drop:    { border:'2px dashed #1e3a5f', borderRadius:10, padding:36, textAlign:'center',
             cursor:'pointer', transition:'all .2s', marginBottom:14 },
  dropA:   { borderColor:'#38bdf8', background:'rgba(56,189,248,.04)' },
  btn:     { padding:'9px 18px', background:'linear-gradient(135deg,#0ea5e9,#38bdf8)',
             border:'none', borderRadius:8, color:'#fff', fontSize:13, fontWeight:700 },
  bsm:     { padding:'6px 12px', border:'none', borderRadius:6, fontSize:12, fontWeight:600 },
  bdanger: { background:'rgba(239,68,68,.15)', color:'#f87171', border:'1px solid rgba(239,68,68,.3)' },
  bgreen:  { background:'rgba(34,197,94,.15)', color:'#4ade80', border:'1px solid rgba(34,197,94,.3)' },
  bblue:   { background:'rgba(56,189,248,.12)', color:'#38bdf8', border:'1px solid rgba(56,189,248,.25)' },
  table:   { width:'100%', borderCollapse:'collapse' },
  th:      { textAlign:'left', padding:'8px 12px', fontSize:11, fontWeight:700, color:'#64748b',
             textTransform:'uppercase', borderBottom:'1px solid #1e3a5f' },
  td:      { padding:'10px 12px', fontSize:13, color:'#e2e8f0', borderBottom:'1px solid rgba(30,58,95,.5)' },
  badge:   { display:'inline-block', padding:'2px 8px', borderRadius:4, fontSize:10, fontWeight:700 },
  opt:     { marginBottom:14 },
  olabel:  { fontSize:12, color:'#94a3b8', marginBottom:4, display:'block' },
  oinput:  { padding:'7px 10px', background:'#070d1a', border:'1px solid #1e3a5f',
             borderRadius:6, color:'#e2e8f0', fontSize:13, width:140 },
  chk:     { display:'flex', alignItems:'center', gap:8, fontSize:13, color:'#94a3b8', cursor:'pointer' },
  toast:   { position:'fixed', bottom:24, right:24, background:'#0d1b2a',
             border:'1px solid #38bdf8', borderRadius:10, padding:'12px 18px',
             color:'#e2e8f0', fontSize:13, zIndex:999, maxWidth:340, lineHeight:1.5 },
  err:     { background:'rgba(239,68,68,.1)', border:'1px solid rgba(239,68,68,.3)',
             borderRadius:8, padding:'10px 14px', color:'#f87171', fontSize:13, marginBottom:12 },
  modal:   { position:'fixed', inset:0, background:'rgba(0,0,0,.75)',
             display:'flex', alignItems:'center', justifyContent:'center', zIndex:200 },
  minput:  { width:'100%', padding:'9px 12px', background:'#070d1a', border:'1px solid #1e3a5f',
             borderRadius:7, color:'#e2e8f0', fontSize:13, outline:'none', marginBottom:12 },
  ubot:    { marginTop:'auto', padding:'14px 20px', borderTop:'1px solid #1e3a5f' },
  uname:   { fontSize:13, color:'#e2e8f0', fontWeight:600 },
  urole:   { fontSize:11, color:'#64748b', marginTop:2 },
};

const SCAN_COLORS = {
  CLEAN:    ['#4ade80','#052e16'],
  INFECTED: ['#f87171','#2d0a0a'],
  PENDING:  ['#fbbf24','#2d1f00'],
  SKIPPED:  ['#94a3b8','#1e293b'],
};

function ScanBadge({ s }) {
  const [fg,bg] = SCAN_COLORS[s] || SCAN_COLORS.PENDING;
  return <span style={{...C.badge, color:fg, background:bg}}>{s}</span>;
}

function Toast({ msg, type }) {
  const color = type==='error'?'#f87171': type==='success'?'#4ade80':'#38bdf8';
  return <div style={{...C.toast, borderColor:color, color}}>{msg}</div>;
}

function fmtSize(b) {
  if (b < 1024) return b + ' B';
  if (b < 1048576) return (b/1024).toFixed(1) + ' KB';
  return (b/1048576).toFixed(1) + ' MB';
}

export default function DashboardPage() {
  const { user, logout, getPrivateKey } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab]         = useState('files');
  const [files, setFiles]     = useState([]);
  const [shared, setShared]   = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState('');
  const [drag, setDrag]       = useState(false);
  const [maxDl, setMaxDl]     = useState('');
  const [expiry, setExpiry]   = useState('');
  const [honey, setHoney]     = useState(false);
  const [error, setError]     = useState('');
  const [toast, setToast]     = useState(null);
  const [shareModal, setShareModal] = useState(null);
  const [shareTarget, setShareTarget] = useState('');
  const [shareExp, setShareExp] = useState('24');
  const fileRef = useRef();

  const showToast = (msg, type='info') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 4500);
  };

  const load = async () => {
    setLoading(true);
    try {
      const [a, b] = await Promise.all([fileApi.myFiles(), fileApi.sharedWithMe()]);
      setFiles(a.data); setShared(b.data);
    } catch { showToast('Failed to load files', 'error'); }
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  // ── Upload ──────────────────────────────────────────────────────────────────
  const handleUpload = async file => {
    if (!file) return;
    setUploading(true); setError('');
    try {
      // 1. SHA-256 of original file
      setUploadMsg('Computing SHA-256 hash…');
      const originalBytes = new Uint8Array(await file.arrayBuffer());
      const hash = await sha256Hash(originalBytes);

      // 2. Generate AES-256-GCM key and encrypt file in browser
      setUploadMsg('Encrypting file with AES-256-GCM…');
      const aesKey = await generateAesKey();
      const { encryptedData, iv } = await encryptFile(file, aesKey);
      const aesKeyBytes = await exportAesKey(aesKey);

      // 3. Wrap AES key with user's RSA public key
      setUploadMsg('Wrapping key with RSA-2048…');
      const pubKey   = await importPublicKey(user.publicKey);
      const wrappedKey = await wrapAesKeyWithPublicKey(aesKeyBytes, pubKey);

      // 4. Send to server (server never sees plaintext or raw AES key)
      setUploadMsg('Uploading encrypted file…');
      const form = new FormData();
      form.append('file', new Blob([encryptedData], { type:'application/octet-stream' }), file.name);
      form.append('encryptedAesKey', bytesToBase64(wrappedKey));
      form.append('aesIv', bytesToBase64(iv));
      form.append('sha256Hash', hash);
      if (maxDl)  form.append('maxDownloads', maxDl);
      if (expiry) form.append('expiryHours', expiry);
      form.append('honeyFile', honey);

      await fileApi.upload(form);
      showToast('✅ File encrypted and uploaded!', 'success');
      setMaxDl(''); setExpiry(''); setHoney(false);
      load();
    } catch (e) {
      setError('Upload failed: ' + (e.response?.data?.message || e.message));
    } finally {
      setUploading(false); setUploadMsg('');
    }
  };

  // ── Download ────────────────────────────────────────────────────────────────
  const handleDownload = async (fileId, filename, mimeType) => {
    try {
      const pkBytes = getPrivateKey();
      if (!pkBytes) { showToast('Private key not in session. Please log out and log back in.', 'error'); return; }

      showToast('⏳ Decrypting…');
      const res = await fileApi.download(fileId);
      const d   = res.data;

      // Import RSA private key
      const privateKey = await importPrivateKey(pkBytes);

      // Unwrap AES key using RSA private key
      const encKeyBytes = base64ToBytes(d.encryptedAesKey);
      let aesKeyBytes;
      try {
        aesKeyBytes = await unwrapAesKeyWithPrivateKey(encKeyBytes, privateKey);
      } catch {
        showToast('❌ Wrong private key — cannot decrypt AES key', 'error'); return;
      }

      // Decrypt file
      const aesKey  = await importAesKey(aesKeyBytes);
      const encData = base64ToBytes(d.encryptedData);
      const iv      = base64ToBytes(d.aesIv);
      const decrypted = await decryptFile(encData, aesKey, iv);

      // Verify SHA-256 integrity
      const ok = await verifyIntegrity(decrypted, d.sha256Hash);
      if (!ok) { showToast('🚨 INTEGRITY CHECK FAILED — file may be tampered!', 'error'); return; }

      triggerDownload(decrypted, d.filename, d.mimeType || mimeType);
      showToast('✅ Decrypted & downloaded. SHA-256 integrity verified!', 'success');
    } catch (e) {
      showToast('Download failed: ' + e.message, 'error');
    }
  };

  // ── Share ───────────────────────────────────────────────────────────────────
  const handleShare = async () => {
    if (!shareTarget.trim()) return;
    try {
      showToast('⏳ Preparing secure share…');

      // Step 1: Get MY private key from session
      const myPrivKeyBytes = getPrivateKey();
      if (!myPrivKeyBytes) {
        showToast('❌ Your private key is not loaded. Please log out and log back in.', 'error');
        return;
      }

      // Step 2: Fetch the RECIPIENT's public key from server
      // Public keys are safe to fetch — they are meant to be shared
      const pubKeyRes = await import('../services/api').then(m => m.default.get('/auth/public-key/' + shareTarget.trim()));
      const recipientPublicKeyB64 = pubKeyRes.data.publicKey;
      if (!recipientPublicKeyB64) {
        showToast('❌ Could not find public key for user: ' + shareTarget, 'error');
        return;
      }

      // Step 3: Download the file's encrypted AES key (which is locked with MY public key)
      const dlRes = await fileApi.download(shareModal.id);
      const encAesKeyB64 = dlRes.data.encryptedAesKey;

      // Step 4: Decrypt the AES key using MY private key
      const myPrivKey = await importPrivateKey(myPrivKeyBytes);
      const encAesKeyBytes = base64ToBytes(encAesKeyB64);
      let aesKeyBytes;
      try {
        aesKeyBytes = await unwrapAesKeyWithPrivateKey(encAesKeyBytes, myPrivKey);
      } catch {
        showToast('❌ Could not decrypt AES key with your private key', 'error');
        return;
      }

      // Step 5: Re-encrypt the AES key with the RECIPIENT's public key
      // Now only the recipient's private key can open it
      const recipientPubKey = await importPublicKey(recipientPublicKeyB64);
      const recipientEncAesKeyBytes = await wrapAesKeyWithPublicKey(aesKeyBytes, recipientPubKey);
      const recipientEncAesKeyB64 = bytesToBase64(recipientEncAesKeyBytes);

      // Step 6: Send the share request with the re-encrypted key
      await fileApi.share(shareModal.id, {
        targetUsername: shareTarget.trim(),
        permission: 'VIEWER',
        expiryHours: shareExp || undefined,
        recipientEncryptedAesKey: recipientEncAesKeyB64,
      });

      showToast('✅ Shared with ' + shareTarget + '. They can now decrypt the file.', 'success');
      setShareModal(null);
      setShareTarget('');
    } catch (e) {
      showToast('Share failed: ' + (e.response?.data?.message || e.message), 'error');
    }
  };

  // ── Delete ──────────────────────────────────────────────────────────────────
  const handleDelete = async id => {
    if (!confirm('Permanently delete this file?')) return;
    try { await fileApi.delete(id); showToast('Deleted', 'success'); load(); }
    catch (e) { showToast('Delete failed: ' + e.message, 'error'); }
  };

  const navItems = [
    { key:'files',  icon:'📁', label:'My Files' },
    { key:'shared', icon:'🤝', label:'Shared With Me' },
    { key:'upload', icon:'⬆️', label:'Upload File' },
  ];

  const FileTable = ({ rows, showActions }) => (
    rows.length === 0
      ? <div style={{textAlign:'center', padding:40, color:'#64748b'}}>No files yet.</div>
      : <table style={C.table}>
          <thead><tr>
            <th style={C.th}>File</th><th style={C.th}>Size</th>
            <th style={C.th}>Scan</th><th style={C.th}>Downloads</th>
            <th style={C.th}>Expires</th><th style={C.th}>Actions</th>
          </tr></thead>
          <tbody>
            {rows.map(f => (
              <tr key={f.id}>
                <td style={C.td}>
                  <div style={{fontWeight:500}}>{f.filename}</div>
                  {f.honeyFile && <span style={{...C.badge, color:'#fbbf24', background:'rgba(245,158,11,.1)', fontSize:9, marginTop:3}}>🍯 HONEY</span>}
                </td>
                <td style={C.td}>{fmtSize(f.fileSize)}</td>
                <td style={C.td}><ScanBadge s={f.scanStatus} /></td>
                <td style={C.td}>{f.downloadCount}{f.maxDownloads ? '/'+f.maxDownloads : ''}</td>
                <td style={C.td}>{f.expiresAt ? new Date(f.expiresAt).toLocaleDateString() : '—'}</td>
                <td style={C.td}>
                  <div style={{display:'flex', gap:5}}>
                    <button style={{...C.bsm,...C.bblue}} onClick={() => handleDownload(f.id, f.filename, f.mimeType)}>⬇ Get</button>
                    {showActions && <>
                      <button style={{...C.bsm,...C.bgreen}} onClick={() => { setShareModal(f); setShareTarget(''); }}>🤝</button>
                      <button style={{...C.bsm,...C.bdanger}} onClick={() => handleDelete(f.id)}>🗑</button>
                    </>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
  );

  return (
    <div style={C.wrap}>
      {/* Sidebar */}
      <div style={C.side}>
        <div style={C.slogo}>
          <div style={C.slgtx}>🔐 SecureShare</div>
          <div style={C.slgsb}>Zero-Trust Platform</div>
        </div>

        {navItems.map(n => (
          <button key={n.key} style={{...C.nav, ...(tab===n.key?C.navA:{})}} onClick={() => setTab(n.key)}>
            {n.icon} {n.label}
          </button>
        ))}
        <Link to="/audit" style={{...C.nav, textDecoration:'none', color:'#94a3b8', display:'flex', alignItems:'center', gap:10}}>
          📋 Audit Logs
        </Link>

        <div style={C.ubot}>
          <div style={C.uname}>👤 {user?.username}</div>
          <div style={C.urole}>{user?.role}</div>
          <button style={{...C.bsm,...C.bdanger, marginTop:10, width:'100%'}}
            onClick={() => { logout(); navigate('/login'); }}>
            Logout
          </button>
        </div>
      </div>

      {/* Main */}
      <div style={C.main}>

        {/* Upload tab */}
        {tab === 'upload' && (
          <div>
            <div style={C.hdr}><h1 style={C.h1}>Upload Encrypted File</h1></div>
            <div style={C.card}>
              <div style={C.ctitle}>🔒 Client-Side Encryption</div>

              <div style={{...C.drop, ...(drag?C.dropA:{})}}
                onDragOver={e=>{e.preventDefault();setDrag(true)}}
                onDragLeave={()=>setDrag(false)}
                onDrop={e=>{e.preventDefault();setDrag(false);handleUpload(e.dataTransfer.files[0])}}
                onClick={()=>fileRef.current?.click()}>
                <div style={{fontSize:42, marginBottom:12}}>🔐</div>
                <div style={{color:'#e2e8f0', fontWeight:600, marginBottom:4}}>
                  {uploading ? uploadMsg || 'Processing…' : 'Drop file here or click to select'}
                </div>
                <div style={{fontSize:12, color:'#64748b'}}>
                  Encrypted with AES-256-GCM in your browser before upload. Server never sees plaintext.
                </div>
                <input ref={fileRef} type="file" style={{display:'none'}}
                  onChange={e=>handleUpload(e.target.files[0])} />
              </div>

              {error && <div style={C.err}>{error}</div>}

              <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginBottom:14}}>
                <div style={C.opt}>
                  <label style={C.olabel}>⏱ Expiry (hours)</label>
                  <input style={C.oinput} type="number" placeholder="e.g. 24"
                    value={expiry} onChange={e=>setExpiry(e.target.value)} min="1" />
                </div>
                <div style={C.opt}>
                  <label style={C.olabel}>📥 Max downloads</label>
                  <input style={C.oinput} type="number" placeholder="e.g. 1"
                    value={maxDl} onChange={e=>setMaxDl(e.target.value)} min="1" />
                </div>
              </div>

              <label style={C.chk}>
                <input type="checkbox" checked={honey} onChange={e=>setHoney(e.target.checked)} />
                🍯 Mark as Honey File (alerts admin if accessed by non-owner)
              </label>
            </div>

            <div style={C.card}>
              <div style={C.ctitle}>🔐 Encryption Pipeline</div>
              <div style={{fontSize:13, color:'#94a3b8', lineHeight:2}}>
                {['SHA-256 hash computed on original file (tamper detection)',
                  'AES-256-GCM key generated in your browser',
                  'File encrypted locally — only ciphertext sent to server',
                  'AES key wrapped with your RSA-2048 public key',
                  'Server stores encrypted blob — cannot decrypt without your private key',
                ].map((t,i) => (
                  <div key={i} style={{display:'flex', gap:10}}>
                    <span style={{color:'#38bdf8', fontWeight:700}}>{i+1}.</span> {t}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* My Files tab */}
        {tab === 'files' && (
          <div>
            <div style={C.hdr}>
              <h1 style={C.h1}>My Files ({files.length})</h1>
              <button style={C.btn} onClick={()=>setTab('upload')}>+ Upload</button>
            </div>
            <div style={C.card}>
              {loading ? <div style={{color:'#64748b',textAlign:'center',padding:32}}>Loading…</div>
                       : <FileTable rows={files} showActions />}
            </div>
          </div>
        )}

        {/* Shared With Me tab */}
        {tab === 'shared' && (
          <div>
            <div style={C.hdr}><h1 style={C.h1}>Shared With Me ({shared.length})</h1></div>
            <div style={C.card}>
              {loading ? <div style={{color:'#64748b',textAlign:'center',padding:32}}>Loading…</div>
                       : <FileTable rows={shared} showActions={false} />}
            </div>
          </div>
        )}
      </div>

      {/* Share Modal */}
      {shareModal && (
        <div style={C.modal}>
          <div style={{...C.card, width:360, marginBottom:0}}>
            <div style={C.ctitle}>🤝 Share "{shareModal.filename}"</div>
            <label style={C.olabel}>Recipient username</label>
            <input style={C.minput} placeholder="Enter username"
              value={shareTarget} onChange={e=>setShareTarget(e.target.value)} autoFocus />
            <label style={C.olabel}>Expires after (hours)</label>
            <input style={C.minput} type="number" value={shareExp} onChange={e=>setShareExp(e.target.value)} min="1" />
            <div style={{display:'flex', gap:8, marginTop:4}}>
              <button style={C.btn} onClick={handleShare}>Share File</button>
              <button style={{...C.bsm,...C.bdanger, padding:'9px 16px'}} onClick={()=>setShareModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {toast && <Toast msg={toast.msg} type={toast.type} />}
    </div>
  );
}
