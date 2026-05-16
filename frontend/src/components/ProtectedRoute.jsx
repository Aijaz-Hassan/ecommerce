import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAdminRole } from "../utils/roles";

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { isAuthenticated, sessionChecked, user } = useAuth();
  const location = useLocation();

  if (!sessionChecked) {
    return <div className="loading-panel">Checking your session...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (adminOnly && !isAdminRole(user?.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}
