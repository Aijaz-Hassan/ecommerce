import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/client";

const AuthContext = createContext(null);

const storageKeys = {
  token: "lumenlane_token",
  user: "lumenlane_user"
};

const buildUser = (data) => {
  const source = data?.user || data || {};
  return {
    id: source.id,
    fullName: source.fullName,
    email: source.email,
    role: source.role,
    phoneNumber: source.phoneNumber || "",
    profilePictureUrl: source.profilePictureUrl || "",
    addressLine1: source.addressLine1 || "",
    addressLine2: source.addressLine2 || "",
    city: source.city || "",
    state: source.state || "",
    postalCode: source.postalCode || "",
    country: source.country || ""
  };
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem(storageKeys.user);
    try {
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      localStorage.removeItem(storageKeys.user);
      return null;
    }
  });
  const [sessionChecked, setSessionChecked] = useState(false);

  useEffect(() => {
    if (user) {
      localStorage.setItem(storageKeys.user, JSON.stringify(user));
    } else {
      localStorage.removeItem(storageKeys.user);
    }
  }, [user]);

  useEffect(() => {
    const token = localStorage.getItem(storageKeys.token);
    if (!token) {
      setUser(null);
      setSessionChecked(true);
      return;
    }

    let ignore = false;
    api
      .get("/auth/me")
      .then((response) => {
        if (!ignore) {
          setUser(buildUser(response.data));
        }
      })
      .catch(() => {
        if (!ignore) {
          logout();
        }
      })
      .finally(() => {
        if (!ignore) {
          setSessionChecked(true);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    const handleExpiredSession = () => {
      logout();
    };

    window.addEventListener("lumenlane:session-expired", handleExpiredSession);
    return () => window.removeEventListener("lumenlane:session-expired", handleExpiredSession);
  }, []);

  const login = async (values) => {
    const response = await api.post("/auth/login", values);
    const nextUser = buildUser(response.data);
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

  const updateProfile = async (values) => {
    const response = await api.put("/auth/me", values);
    const nextUser = buildUser(response.data);
    setUser(nextUser);
    return nextUser;
  };

  const resetPassword = async (values) => {
    await api.post("/auth/forgot-password", values);
  };

  const logout = () => {
    localStorage.removeItem(storageKeys.token);
    localStorage.removeItem(storageKeys.user);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        register,
        logout,
        updateProfile,
        resetPassword,
        isAuthenticated: Boolean(user),
        sessionChecked
      }}
    >
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
