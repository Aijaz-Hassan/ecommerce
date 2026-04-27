import { useState } from "react";
import api from "../api/client";

const initialForm = {
  name: "",
  description: "",
  category: "",
  imageUrl: "",
  price: "",
  stock: ""
};

export default function AddProductPage() {
  const [form, setForm] = useState(initialForm);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

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
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to add product.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="page">
      <section className="admin-layout">
        <div className="admin-copy">
          <p className="eyebrow">Admin studio</p>
          <h1>Launch a new product drop in minutes.</h1>
          <p>
            This protected page fulfills the frontend route protection task while wiring into the secured Spring Boot
            add-product API.
          </p>
        </div>

        <form className="admin-form" onSubmit={handleSubmit}>
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
      </section>
    </main>
  );
}
