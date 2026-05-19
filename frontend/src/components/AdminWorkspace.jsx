import {
  BarChart3,
  Bell,
  Boxes,
  CreditCard,
  Gauge,
  Gift,
  Layers3,
  LogOut,
  MessageSquare,
  Moon,
  Package,
  ReceiptText,
  Search,
  Settings,
  ShoppingBag,
  Star,
  Sun,
  Users
} from "lucide-react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const adminNavItems = [
  { to: "/admin", label: "Dashboard", icon: Gauge },
  { to: "/admin/products", label: "Products Management", icon: Package },
  { to: "/admin/products?section=categories", label: "Categories Management", icon: Layers3 },
  { to: "/admin/orders", label: "Orders Management", icon: ReceiptText },
  { to: "/admin/users", label: "Customers Management", icon: Users },
  { to: "/admin/orders?section=inventory", label: "Inventory Management", icon: Boxes },
  { to: "/admin/orders?section=payments", label: "Payments", icon: CreditCard },
  { to: "/admin?section=analytics", label: "Analytics / Reports", icon: BarChart3 },
  { to: "/admin/products?section=coupons", label: "Discounts / Coupons", icon: Gift },
  { to: "/admin/products?section=reviews", label: "Reviews & Ratings", icon: Star },
  { to: "/settings", label: "Settings", icon: Settings }
];

export default function AdminWorkspace({ title, subtitle, search, onSearchChange, children, actions }) {
  const { logout, updateSettings, user } = useAuth();
  const navigate = useNavigate();
  const isDark = Boolean(user?.darkModeEnabled);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const handleThemeToggle = async () => {
    try {
      await updateSettings({
        language: user?.language || "en",
        darkModeEnabled: !isDark,
        orderNotificationsEnabled: user?.orderNotificationsEnabled ?? true,
        marketingNotificationsEnabled: Boolean(user?.marketingNotificationsEnabled)
      });
    } catch {
      document.body.classList.toggle("dark-mode", !isDark);
    }
  };

  const initials = (user?.fullName || user?.email || "AD")
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <main className="admin-workspace">
      <aside className="admin-sidebar">
        <Link className="admin-sidebar-brand" to="/admin">
          <span>LL</span>
          <strong>Admin Center</strong>
        </Link>

        <nav className="admin-sidebar-nav" aria-label="Admin navigation">
          {adminNavItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.label} to={item.to} className={({ isActive }) => (isActive ? "active" : "")}>
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <button className="admin-sidebar-logout" type="button" onClick={handleLogout}>
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </aside>

      <section className="admin-main-panel">
        <header className="admin-topbar">
          <label className="admin-search">
            <Search size={18} />
            <input
              value={search || ""}
              onChange={(event) => onSearchChange?.(event.target.value)}
              placeholder="Search dashboard, products, orders, customers"
            />
          </label>

          <div className="admin-topbar-actions">
            <button className="admin-icon-action" type="button" aria-label="Notifications">
              <Bell size={18} />
              <span />
            </button>
            <button className="admin-icon-action" type="button" aria-label="Messages">
              <MessageSquare size={18} />
            </button>
            <button className="admin-icon-action" type="button" onClick={handleThemeToggle} aria-label="Toggle theme">
              {isDark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="admin-profile-chip">
              {user?.profilePictureUrl ? <img src={user.profilePictureUrl} alt={user.fullName} /> : <span>{initials}</span>}
              <div>
                <strong>{user?.fullName || "Admin"}</strong>
                <small>Store administrator</small>
              </div>
            </div>
          </div>
        </header>

        <section className="admin-page-title">
          <div>
            <p className="eyebrow">Lumen Lane admin</p>
            <h1>{title}</h1>
            <p>{subtitle}</p>
          </div>
          {actions && <div className="admin-title-actions">{actions}</div>}
        </section>

        {children}
      </section>
    </main>
  );
}
