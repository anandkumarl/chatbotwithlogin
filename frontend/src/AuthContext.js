import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();
const AUTH_API = 'http://localhost:8081/api/auth';

const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
};

const setAuthToken = (token) => {
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete axios.defaults.headers.common['Authorization'];
  }
};

const getUsernameFromToken = (token) => {
  const payload = parseJwt(token);
  return payload?.sub || payload?.username || null;
};

const isTokenValid = (token) => {
  const payload = parseJwt(token);
  if (!payload) return false;
  if (!payload.exp) return true;
  return Math.floor(Date.now() / 1000) < payload.exp;
};

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && isTokenValid(token)) {
      setAuthToken(token);
      const username = getUsernameFromToken(token);
      if (username) {
        setUser({ username });
      }
    } else {
      localStorage.removeItem('token');
      setAuthToken(null);
      setUser(null);
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const response = await axios.post(`${AUTH_API}/login`, { username, password });
      const { token, username: userUsername } = response.data;
      localStorage.setItem('token', token);
      setAuthToken(token);
      setUser({ username: userUsername || getUsernameFromToken(token) });
      return { success: true };
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Login failed' };
    }
  };

  const register = async (username, email, password) => {
    try {
      const response = await axios.post(`${AUTH_API}/register`, { username, email, password });
      const { token, username: userUsername } = response.data;
      localStorage.setItem('token', token);
      setAuthToken(token);
      setUser({ username: userUsername || getUsernameFromToken(token) });
      return { success: true };
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Registration failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setAuthToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};
