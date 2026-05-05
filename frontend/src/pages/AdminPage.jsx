import { useEffect, useState } from "react";
import api from "../api/client";

const initialForm = {
  id: null,
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
  const [carts, setCarts] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(true);

  const loadAdminData = async () => {
    setRefreshing(true);
    try {
      const [productsResponse, usersResponse, cartsResponse] = await Promise.all([
        api.get("/products"),
        api.get("/auth/users"),
        api.get("/cart/admin-summary")
      ]);
      setProducts(productsResponse.data);
      setUsers(usersResponse.data);
      setCarts(cartsResponse.data);
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
      const payload = {
        ...form,
        price: Number(form.price),
        stock: Number(form.stock)
      };
      delete payload.id;

      if (form.id) {
        await api.put(`/products/${form.id}`, payload);
        setMessage("Product updated successfully.");
      } else {
        await api.post("/products", payload);
        setMessage("Product created successfully.");
      }
      setForm(initialForm);
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to save product.");
    } finally {
      setLoading(false);
    }
  };

  const handleEditProduct = (product) => {
    setForm({
      id: product.id,
      name: product.name,
      description: product.description,
      category: product.category,
      imageUrl: product.imageUrl,
      price: product.price,
      stock: product.stock
    });
    setMessage("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
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
          <h2>{form.id ? "Update Product" : "Add Product"}</h2>
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
          <div className="admin-form-actions full-span">
            <button className="solid-button" type="submit" disabled={loading}>
              {loading ? "Saving..." : form.id ? "Update Product" : "Add Product"}
            </button>
            {form.id && (
              <button className="ghost-button" type="button" onClick={() => setForm(initialForm)}>
                Cancel edit
              </button>
            )}
          </div>
        </form>

        <section className="admin-lists">
          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>Customer carts</h2>
            </div>
            <div className="admin-table">
              {carts.length === 0 ? (
                <article className="admin-row">
                  <div>
                    <strong>No carts yet</strong>
                    <p>Customer cart items and totals will appear here once users start adding products.</p>
                  </div>
                </article>
              ) : (
                carts.map((cart) => (
                  <article className="admin-row admin-order-row" key={cart.cartId}>
                    <div>
                      <strong>{cart.customerName}</strong>
                      <p>{cart.customerEmail}</p>
                    </div>
                    <div>
                      <strong>Products</strong>
                      <p>{cart.items.length ? cart.items.map((item) => `${item.productName} x${item.quantity}`).join(", ") : "Cart is empty"}</p>
                    </div>
                    <div>
                      <strong>Amount</strong>
                      <p>${Number(cart.totalAmount).toFixed(2)}</p>
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
                  <div className="admin-row-actions">
                    <button className="ghost-button" type="button" onClick={() => handleEditProduct(product)}>
                      Update
                    </button>
                    <button className="danger-button" type="button" onClick={() => handleDeleteProduct(product.id)}>
                      Delete
                    </button>
                  </div>
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
