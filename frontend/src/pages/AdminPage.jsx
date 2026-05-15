import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/client";
import AdminSectionMenu from "../components/AdminSectionMenu";

export default function AdminPage() {
  const [stats, setStats] = useState({
    products: 0,
    users: 0,
    orders: 0
  });
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadOverview() {
      try {
        const [productsResponse, usersResponse, ordersResponse] = await Promise.all([
          api.get("/products"),
          api.get("/auth/users"),
          api.get("/orders/admin-summary")
        ]);

        if (!ignore) {
          setStats({
            products: (productsResponse.data || []).length,
            users: (usersResponse.data || []).length,
            orders: (ordersResponse.data || []).length
          });
          setError("");
        }
      } catch (requestError) {
        if (!ignore) {
          setError(requestError.response?.data?.message || "Unable to load admin overview.");
        }
      }
    }

    loadOverview();
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <main className="page">
      <section className="admin-header">
        <div>
          <p className="eyebrow">Admin dashboard</p>
          <h1>Use the dropdown to move across separate admin pages.</h1>
          <p className="section-note">Products, users, and customer purchases each now have their own screen.</p>
        </div>
      </section>

      <AdminSectionMenu value="/admin" />

      {error && <p className="error-text">{error}</p>}

      <section className="stats-band">
        <article>
          <span>Products</span>
          <strong>{stats.products}</strong>
        </article>
        <article>
          <span>Users</span>
          <strong>{stats.users}</strong>
        </article>
        <article>
          <span>Customer orders</span>
          <strong>{stats.orders}</strong>
        </article>
      </section>

      <section className="highlights-grid">
        <article className="feature-panel">
          <h2>Products</h2>
          <p>Add products, update price and stock, and remove items from the catalog.</p>
          <Link className="solid-link" to="/admin/products">
            Open products
          </Link>
        </article>
        <article className="feature-panel">
          <h2>Users</h2>
          <p>Review customer accounts and remove users from a dedicated admin screen.</p>
          <Link className="solid-link" to="/admin/users">
            Open users
          </Link>
        </article>
        <article className="feature-panel">
          <h2>Customer orders</h2>
          <p>See purchased orders and remaining stock so you can track product movement properly.</p>
          <Link className="solid-link" to="/admin/orders">
            Open orders
          </Link>
        </article>
      </section>
    </main>
  );
}
