import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { auditApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const S = {
  page:  { minHeight:'100vh', background:'#070d1a', padding:28 },
  hdr:   { display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:22 },
  h1:    { fontSize:22, fontWeight:700, color:'#e2e8f0' },
  sub:   { fontSize:13, color:'#64748b', marginTop:4 },
  card:  { background:'#0d1b2a', border:'1px solid #1e3a5f', borderRadius:12, padding:20, marginBottom:16 },
  table: { width:'100%', borderCollapse:'collapse' },
  th:    { textAlign:'left', padding:'8px 12px', fontSize:11, fontWeight:700, color:'#64748b',
           textTransform:'uppercase', borderBottom:'1px solid #1e3a5f' },
  td:    { padding:'10px 12px', fontSize:12, color:'#e2e8f0', borderBottom:'1px solid rgba(30,58,95,.5)',
           fontFamily:'monospace' },
  badge: { display:'inline-block', padding:'2px 8px', borderRadius:4, fontSize:10, fontWeight:700 },
  btn:   { padding:'8px 16px', background:'linear-gradient(135deg,#0ea5e9,#38bdf8)',
           border:'none', borderRadius:8, color:'#fff', fontSize:13, fontWeight:700, cursor:'pointer' },
  back:  { padding:'8px 16px', background:'#1e3a5f', border:'none', borderRadius:8,
           color:'#e2e8f0', fontSize:13, cursor:'pointer', textDecoration:'none', display:'inline-block' },
  banner:{ borderRadius:10, padding:'14px 20px', marginBottom:18, fontSize:14, fontWeight:600 },
  tabs:  { display:'flex', gap:8, marginBottom:18 },
  tab:   { padding:'7px 16px', borderRadius:8, border:'1px solid #1e3a5f', background:'transparent',
           color:'#94a3b8', fontSize:13, cursor:'pointer' },
  tabA:  { background:'rgba(56,189,248,.1)', borderColor:'#38bdf8', color:'#38bdf8' },
};

const ACTION_COLORS = {
  USER_REGISTERED:    ['#4ade80','#052e16'],
  USER_LOGIN:         ['#60a5fa','#0c1a35'],
  USER_LOGIN_FAILED:  ['#f87171','#2d0a0a'],
  USER_LOCKED:        ['#f97316','#2d1500'],
  FILE_UPLOADED:      ['#a78bfa','#1a0d35'],
  FILE_DOWNLOADED:    ['#34d399','#051a10'],
  FILE_SHARED:        ['#38bdf8','#0a1f2d'],
  FILE_DELETED:       ['#f87171','#2d0a0a'],
  FILE_EXPIRED:       ['#94a3b8','#1e293b'],
  HONEY_FILE_ACCESSED:['#fbbf24','#2d1f00'],
  SUSPICIOUS_ACTIVITY:['#f97316','#2d1500'],
  MALWARE_DETECTED:   ['#ef4444','#2d0808'],
};

function ActionBadge({ action }) {
  const [fg, bg] = ACTION_COLORS[action] || ['#94a3b8','#1e293b'];
  return <span style={{...S.badge, color:fg, background:bg}}>{action?.replace(/_/g,' ')}</span>;
}

export default function AuditPage() {
  const { user } = useAuth();
  const [logs, setLogs]     = useState([]);
  const [chain, setChain]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab]       = useState('my');

  const load = async () => {
    setLoading(true);
    try {
      const res = tab === 'my' ? await auditApi.myLogs() : await auditApi.allLogs();
      setLogs(res.data);
    } catch { setLogs([]); }
    setLoading(false);
  };

  useEffect(() => { load(); }, [tab]);

  const verifyChain = async () => {
    try {
      const r = await auditApi.verifyChain();
      setChain(r.data);
    } catch (e) {
      setChain({ chainIntact: false, message: 'Verification failed: ' + e.message });
    }
  };

  return (
    <div style={S.page}>
      <div style={S.hdr}>
        <div>
          <h1 style={S.h1}>📋 Tamper-Proof Audit Logs</h1>
          <div style={S.sub}>SHA-256 hash chain — any modification is cryptographically detectable</div>
        </div>
        <div style={{display:'flex', gap:10, flexWrap:'wrap'}}>
          {user?.role === 'ADMIN' && (
            <button style={S.btn} onClick={verifyChain}>🔍 Verify Chain Integrity</button>
          )}
          <Link to="/dashboard" style={S.back}>← Dashboard</Link>
        </div>
      </div>

      {chain && (
        <div style={{...S.banner,
          background: chain.chainIntact ? 'rgba(34,197,94,.1)' : 'rgba(239,68,68,.1)',
          border: '1px solid ' + (chain.chainIntact ? 'rgba(34,197,94,.3)' : 'rgba(239,68,68,.3)'),
          color: chain.chainIntact ? '#4ade80' : '#f87171',
        }}>
          {chain.chainIntact ? '✅' : '🚨'} {chain.message}
        </div>
      )}

      {user?.role === 'ADMIN' && (
        <div style={S.tabs}>
          <button style={{...S.tab,...(tab==='my'?S.tabA:{})}} onClick={()=>setTab('my')}>My Logs</button>
          <button style={{...S.tab,...(tab==='all'?S.tabA:{})}} onClick={()=>setTab('all')}>All Logs (Admin)</button>
        </div>
      )}

      <div style={S.card}>
        {loading
          ? <div style={{color:'#64748b',textAlign:'center',padding:32}}>Loading audit logs…</div>
          : logs.length === 0
            ? <div style={{color:'#64748b',textAlign:'center',padding:40}}>No logs yet.</div>
            : <table style={S.table}>
                <thead><tr>
                  <th style={S.th}>Time</th>
                  <th style={S.th}>Action</th>
                  <th style={S.th}>User</th>
                  <th style={S.th}>File</th>
                  <th style={S.th}>IP</th>
                  <th style={S.th}>Details</th>
                  <th style={S.th}>Hash (prefix)</th>
                </tr></thead>
                <tbody>
                  {logs.map(l => (
                    <tr key={l.id}>
                      <td style={S.td}>{new Date(l.timestamp).toLocaleString()}</td>
                      <td style={S.td}><ActionBadge action={l.action} /></td>
                      <td style={S.td}>{l.actorUsername}</td>
                      <td style={S.td}>{l.filename || '—'}</td>
                      <td style={S.td}>{l.ipAddress || '—'}</td>
                      <td style={{...S.td, maxWidth:180, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
                        {l.details || '—'}
                      </td>
                      <td style={{...S.td, color:'#64748b', fontSize:10}}>
                        {l.recordHash?.slice(0,16)}…
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
        }
      </div>

      <div style={{...S.card, fontSize:12, color:'#64748b', lineHeight:1.9}}>
        <strong style={{color:'#94a3b8'}}>🔗 How the Hash Chain Works:</strong><br/>
        Each log entry contains <code style={{color:'#38bdf8'}}>SHA-256(prev_hash + record_data)</code>.
        If any past log is modified, all subsequent hashes become invalid — tampering is instantly
        detectable by verifying the full chain. Click "Verify Chain Integrity" to check all logs.
      </div>
    </div>
  );
}
