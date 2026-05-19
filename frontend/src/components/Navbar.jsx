import { useEffect, useRef, useState } from "react";
import { Heart, KeyRound, LayoutDashboard, LogOut, Menu, Search, Settings, ShoppingCart, UserCircle, X } from "lucide-react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import ConfirmationModal from "./ConfirmationModal";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { isAdminRole } from "../utils/roles";

export default function Navbar() {
  const { isAuthenticated, logout, user } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const accountRef = useRef(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [accountOpen, setAccountOpen] = useState(false);
  const [logoutModalOpen, setLogoutModalOpen] = useState(false);
  const [search, setSearch] = useState("");
  const isAdmin = isAdminRole(user?.role);

  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (accountRef.current && !accountRef.current.contains(event.target)) {
        setAccountOpen(false);
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  const closeMenus = () => {
    setMenuOpen(false);
    setAccountOpen(false);
  };

  const handleSearch = (event) => {
    event.preventDefault();
    const query = search.trim();
    navigate(query ? `/products?search=${encodeURIComponent(query)}` : "/products");
    closeMenus();
  };

  const handleLogout = () => {
    logout();
    setLogoutModalOpen(false);
    closeMenus();
    navigate("/login");
  };

  const initials = (user?.fullName || user?.email || "LL")
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <>
      <header className="navbar">
        <Link className="brand" to="/" onClick={closeMenus}>
          <span className="brand-mark">LL</span>
          <span>
            <strong>Lumen Lane</strong>
            <small>Premium commerce</small>
          </span>
        </Link>

        <form className="nav-search" onSubmit={handleSearch}>
          <Search size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search products, brands, categories" />
          <button type="submit">Search</button>
        </form>

        <button className="mobile-menu-button" type="button" onClick={() => setMenuOpen((current) => !current)} aria-label="Toggle navigation">
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <nav className={`nav-links${menuOpen ? " open" : ""}`}>
          <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/" onClick={closeMenus}>
            Home
          </NavLink>
          <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/products" onClick={closeMenus}>
            Products
          </NavLink>
          {isAdmin ? (
            <NavLink className={({ isActive }) => (isActive ? "active" : "")} to="/admin" onClick={closeMenus}>
              Admin
            </NavLink>
          ) : (
            <>
              <a href="/#categories" onClick={closeMenus}>
                Categories
              </a>
              <a href="/#about" onClick={closeMenus}>
                About
              </a>
              <a href="/#contact" onClick={closeMenus}>
                Contact
              </a>
            </>
          )}
        </nav>

        <div className={`nav-actions${menuOpen ? " open" : ""}`}>
          {!isAdmin && (
            <>
              <button className="nav-icon-button" type="button" onClick={() => navigate("/products")} aria-label="Wishlist">
                <Heart size={20} />
              </button>
              <button className="nav-icon-button badge-button" type="button" onClick={() => navigate("/cart")} aria-label="Cart">
                <ShoppingCart size={20} />
                <span>{cartCount}</span>
              </button>
            </>
          )}
          {isAuthenticated ? (
            <div className="account-menu" ref={accountRef}>
              <button className="account-button" type="button" onClick={() => setAccountOpen((current) => !current)} aria-expanded={accountOpen}>
                {user.profilePictureUrl ? (
                  <img className="welcome-avatar" src={user.profilePictureUrl} alt={user.fullName} />
                ) : (
                  <span className="welcome-avatar welcome-avatar-fallback">{initials}</span>
                )}
                <span>
                  <strong>{user.fullName}</strong>
                  <small>My Account</small>
                </span>
                <span className={`chevron${accountOpen ? " open" : ""}`}>v</span>
              </button>
              <div className={`account-dropdown${accountOpen ? " open" : ""}`}>
                {isAdmin ? (
                  <Link to="/admin" onClick={closeMenus}>
                    <LayoutDashboard size={17} />
                    Admin
                  </Link>
                ) : (
                  <>
                    <Link to="/profile" onClick={closeMenus}>
                      <UserCircle size={17} />
                      Profile
                    </Link>
                    <Link to="/orders" onClick={closeMenus}>
                      <ShoppingCart size={17} />
                      Orders
                    </Link>
                    <Link to="/settings" onClick={closeMenus}>
                      <Settings size={17} />
                      Settings
                    </Link>
                    <Link to="/account/password" onClick={closeMenus}>
                      <KeyRound size={17} />
                      Password
                    </Link>
                  </>
                )}
                <button type="button" onClick={() => setLogoutModalOpen(true)}>
                  <LogOut size={17} />
                  Logout
                </button>
              </div>
            </div>
          ) : (
            <div className="nav-auth-links">
              <NavLink className={({ isActive }) => `ghost-link${isActive ? " active" : ""}`} to="/login" onClick={closeMenus}>
                Login
              </NavLink>
              <NavLink className={({ isActive }) => `solid-link${isActive ? " active" : ""}`} to="/register" onClick={closeMenus}>
                Register
              </NavLink>
            </div>
          )}
        </div>
      </header>
      <ConfirmationModal
        open={logoutModalOpen}
        title="Log out?"
        message="Your session will end on this device."
        confirmLabel="Logout"
        danger
        onConfirm={handleLogout}
        onCancel={() => setLogoutModalOpen(false)}
      />
    </>
  );
}
