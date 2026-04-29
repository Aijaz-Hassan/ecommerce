import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { isAdminRole } from "../utils/roles";

export default function Navbar() {
  const { isAuthenticated, logout, user } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <Link className="brand" to="/">
        <span className="brand-mark">LL</span>
        <span>
          <strong>Lumen Lane</strong>
          <small>Curated tech-luxe commerce</small>
        </span>
      </Link>

      <nav className="nav-links">
        <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/">
          Home
        </NavLink>
        <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/products">
          Products
        </NavLink>
        <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/cart">
          Cart
        </NavLink>
        {isAdminRole(user?.role) && (
          <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/admin">
            Admin
          </NavLink>
        )}
      </nav>

      <div className="nav-actions">
        <Link className="cart-pill" to={isAuthenticated ? "/cart" : "/login"}>
          <span>Cart</span>
          <strong>{cartCount}</strong>
        </Link>
        {isAuthenticated ? (
          <>
            <div className="welcome-pill">
              <span>{user.fullName}</span>
              <small>{isAdminRole(user.role) ? "Admin" : "Member"}</small>
            </div>
            <button className="logout-button" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <NavLink className={({ isActive }) => `ghost-link${isActive ? " active" : ""}`} to="/login">
              Login
            </NavLink>
            <NavLink className={({ isActive }) => `solid-link${isActive ? " active" : ""}`} to="/register">
              Register
            </NavLink>
          </>
        )}
      </div>
    </header>
  );
}
