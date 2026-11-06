import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const saved = localStorage.getItem('user');
    if (token && saved) {
      try { setUser(JSON.parse(saved)); } catch { localStorage.clear(); }
    }
    setLoading(false);
  }, []);

  // Store private key bytes in sessionStorage (cleared when tab closes)
  const savePrivateKey  = bytes => sessionStorage.setItem('privateKey', JSON.stringify(Array.from(bytes)));
  const getPrivateKey   = () => {
    const s = sessionStorage.getItem('privateKey');
    return s ? new Uint8Array(JSON.parse(s)) : null;
  };

  const login = (userData, token, refreshToken) => {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken || '');
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.clear();
    sessionStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, savePrivateKey, getPrivateKey }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
