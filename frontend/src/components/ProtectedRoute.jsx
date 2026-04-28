import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAdminRole } from "../utils/roles";

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (adminOnly && !isAdminRole(user?.role)) {
    return <Navigate to="/products" replace />;
  }

  return children;
}
