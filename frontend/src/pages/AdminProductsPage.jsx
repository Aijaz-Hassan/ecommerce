import { useEffect, useState } from "react";
import api from "../api/client";
import AdminSectionMenu from "../components/AdminSectionMenu";

const initialForm = {
  id: null,
  name: "",
  description: "",
  category: "",
  imageUrl: "",
  price: "",
  stock: ""
};

export default function AdminProductsPage() {
  const [form, setForm] = useState(initialForm);
  const [products, setProducts] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(true);

  const loadProducts = async () => {
    setRefreshing(true);
    try {
      const response = await api.get("/products");
      setProducts(response.data || []);
      setError("");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to load products.");
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadProducts();
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
      await loadProducts();
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
      await loadProducts();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to delete product.");
    }
  };

  return (
    <main className="page">
      <section className="admin-header">
        <div>
          <p className="eyebrow">Admin products</p>
          <h1>Add, update, and remove products from a dedicated page.</h1>
          <p className="section-note">This screen is focused only on the catalog and stock setup.</p>
        </div>
      </section>

      <AdminSectionMenu value="/admin/products" />

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
              <h2>Products</h2>
              <button className="ghost-button" type="button" onClick={loadProducts}>
                {refreshing ? "Refreshing..." : "Refresh"}
              </button>
            </div>
            <div className="admin-table">
              {products.map((product) => (
                <article className="admin-row" key={product.id}>
                  <div>
                    <strong>{product.name}</strong>
                    <p>{product.category}</p>
                    <p>Stock left: {product.stock}</p>
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
        </section>
      </section>
    </main>
  );
}
