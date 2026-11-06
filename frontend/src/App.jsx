import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage    from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AuditPage    from './pages/AuditPage';

function Private({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={S.loading}>Loading SecureShare…</div>;
  return user ? children : <Navigate to="/login" />;
}
function Public({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={S.loading}>Loading…</div>;
  return user ? <Navigate to="/dashboard" /> : children;
}
const S = { loading: { display:'flex', alignItems:'center', justifyContent:'center',
  height:'100vh', background:'#070d1a', color:'#38bdf8', fontSize:18, fontFamily:'monospace' } };

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" />} />
          <Route path="/login"     element={<Public><LoginPage /></Public>} />
          <Route path="/register"  element={<Public><RegisterPage /></Public>} />
          <Route path="/dashboard" element={<Private><DashboardPage /></Private>} />
          <Route path="/audit"     element={<Private><AuditPage /></Private>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
