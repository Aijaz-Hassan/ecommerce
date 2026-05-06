import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/client";

const AuthContext = createContext(null);

const storageKeys = {
  token: "lumenlane_token",
  user: "lumenlane_user"
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem(storageKeys.user);
    return savedUser ? JSON.parse(savedUser) : null;
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem(storageKeys.user, JSON.stringify(user));
    } else {
      localStorage.removeItem(storageKeys.user);
    }
  }, [user]);

  const login = async (values) => {
    const response = await api.post("/auth/login", values);
    const nextUser = {
      fullName: response.data.fullName,
      email: response.data.email,
      role: response.data.role
    };
    localStorage.setItem(storageKeys.token, response.data.token);
    setUser(nextUser);
    return nextUser;
  };

  const register = async (values) => {
    const response = await api.post("/auth/register", values);
    logout();
    return {
      fullName: response.data.fullName,
      email: response.data.email,
      role: response.data.role
    };
  };

  const logout = () => {
    localStorage.removeItem(storageKeys.token);
    localStorage.removeItem(storageKeys.user);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: Boolean(user) }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
