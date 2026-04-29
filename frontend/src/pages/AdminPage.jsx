import { useEffect, useState } from "react";
import api from "../api/client";

const initialForm = {
  name: "",
  description: "",
  category: "",
  imageUrl: "",
  price: "",
  stock: ""
};

export default function AdminPage() {
  const [form, setForm] = useState(initialForm);
  const [products, setProducts] = useState([]);
  const [users, setUsers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(true);

  const loadAdminData = async () => {
    setRefreshing(true);
    try {
      const [productsResponse, usersResponse, ordersResponse] = await Promise.all([
        api.get("/products"),
        api.get("/auth/users"),
        api.get("/orders/admin-summary")
      ]);
      setProducts(productsResponse.data);
      setUsers(usersResponse.data);
      setOrders(ordersResponse.data);
      setError("");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to load admin data.");
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      await api.post("/products", {
        ...form,
        price: Number(form.price),
        stock: Number(form.stock)
      });
      setMessage("Product created successfully.");
      setForm(initialForm);
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to add product.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (productId) => {
    try {
      await api.delete(`/products/${productId}`);
      setMessage("Product deleted successfully.");
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to delete product.");
    }
  };

  const handleDeleteUser = async (userId) => {
    try {
      await api.delete(`/auth/users/${userId}`);
      setMessage("User deleted successfully.");
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to delete user.");
    }
  };

  return (
    <main className="page">
      <section className="admin-header">
        <div>
          <p className="eyebrow">Admin console</p>
          <h1>Manage products and users from one place.</h1>
          <p className="section-note">Create new products, remove old ones, and maintain user access.</p>
        </div>
      </section>

      <section className="admin-console">
        <form className="admin-form" onSubmit={handleSubmit}>
          <h2>Add Product</h2>
          <label>
            Product name
            <input name="name" value={form.name} onChange={handleChange} placeholder="Aurora Noise-Cancel Headset" />
          </label>
          <label>
            Category
            <input name="category" value={form.category} onChange={handleChange} placeholder="Audio" />
          </label>
          <label className="full-span">
            Description
            <textarea
              name="description"
              rows="4"
              value={form.description}
              onChange={handleChange}
              placeholder="Describe the product story, build quality, and standout features"
            />
          </label>
          <label>
            Image URL
            <input name="imageUrl" value={form.imageUrl} onChange={handleChange} placeholder="https://example.com/image.jpg" />
          </label>
          <label>
            Price
            <input name="price" type="number" min="0" step="0.01" value={form.price} onChange={handleChange} placeholder="199.99" />
          </label>
          <label>
            Stock
            <input name="stock" type="number" min="0" value={form.stock} onChange={handleChange} placeholder="25" />
          </label>
          {message && <p className="success-text full-span">{message}</p>}
          {error && <p className="error-text full-span">{error}</p>}
          <button className="solid-button full-span" type="submit" disabled={loading}>
            {loading ? "Publishing..." : "Add Product"}
          </button>
        </form>

        <section className="admin-lists">
          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>Customer purchases</h2>
            </div>
            <div className="admin-table">
              {orders.length === 0 ? (
                <article className="admin-row">
                  <div>
                    <strong>No purchases yet</strong>
                    <p>Completed orders will appear here with customer name, email, products, and amount.</p>
                  </div>
                </article>
              ) : (
                orders.map((order) => (
                  <article className="admin-row admin-order-row" key={order.orderId}>
                    <div>
                      <strong>{order.customerName}</strong>
                      <p>{order.customerEmail}</p>
                    </div>
                    <div>
                      <strong>Products</strong>
                      <p>{order.purchasedProducts.join(", ")}</p>
                    </div>
                    <div>
                      <strong>Amount</strong>
                      <p>${Number(order.totalAmount).toFixed(2)}</p>
                    </div>
                  </article>
                ))
              )}
            </div>
          </div>

          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>Products</h2>
              <button className="ghost-button" type="button" onClick={loadAdminData}>
                {refreshing ? "Refreshing..." : "Refresh"}
              </button>
            </div>
            <div className="admin-table">
              {products.map((product) => (
                <article className="admin-row" key={product.id}>
                  <div>
                    <strong>{product.name}</strong>
                    <p>{product.category}</p>
                  </div>
                  <button className="danger-button" type="button" onClick={() => handleDeleteProduct(product.id)}>
                    Delete
                  </button>
                </article>
              ))}
            </div>
          </div>

          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>Users</h2>
            </div>
            <div className="admin-table">
              {users.map((user) => (
                <article className="admin-row" key={user.id}>
                  <div>
                    <strong>{user.fullName || user.email}</strong>
                    <p>
                      {user.email} | {user.role}
                    </p>
                  </div>
                  <button className="danger-button" type="button" onClick={() => handleDeleteUser(user.id)}>
                    Delete
                  </button>
                </article>
              ))}
            </div>
          </div>
        </section>
      </section>
    </main>
  );
}
