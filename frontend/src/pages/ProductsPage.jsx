import { useEffect, useState } from "react";
import api from "../api/client";

const fallbackProducts = [
  {
    id: "f1",
    name: "Halo Portable Projector",
    description: "Pocket-sized cinema energy for intimate rooms and weekend escapes.",
    category: "Entertainment",
    imageUrl:
      "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=900&q=80",
    price: 299.99,
    stock: 14
  },
  {
    id: "f2",
    name: "Vanta Mechanical Keyboard",
    description: "Low-profile tactile switches and sculpted aluminum for all-day flow.",
    category: "Workspace",
    imageUrl:
      "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=900&q=80",
    price: 189.0,
    stock: 27
  },
  {
    id: "f3",
    name: "Nova Travel Speaker",
    description: "Spatial sound, soft shell finish, and a battery that keeps the mood alive.",
    category: "Audio",
    imageUrl:
      "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
    price: 149.5,
    stock: 33
  }
];

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    async function fetchProducts() {
      try {
        const response = await api.get("/products");
        if (!ignore) {
          const incoming = response.data?.length ? response.data : fallbackProducts;
          setProducts(incoming);
          setStatus("ready");
        }
      } catch (error) {
        if (!ignore) {
          setProducts(fallbackProducts);
          setStatus("ready");
          setMessage("Backend not running yet, so preview products are shown.");
        }
      }
    }

    fetchProducts();
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Product gallery</p>
          <h1>Pieces with presence, utility, and a little drama.</h1>
        </div>
        {message && <p className="section-note">{message}</p>}
      </section>

      {status === "loading" ? (
        <div className="loading-panel">Loading products...</div>
      ) : (
        <section className="products-grid">
          {products.map((product) => (
            <article className="product-card" key={product.id}>
              <div className="product-image-wrap">
                <img src={product.imageUrl} alt={product.name} className="product-image" />
                <span className="category-chip">{product.category}</span>
              </div>
              <div className="product-body">
                <div className="product-header">
                  <h3>{product.name}</h3>
                  <strong>${Number(product.price).toFixed(2)}</strong>
                </div>
                <p>{product.description}</p>
                <div className="product-meta">
                  <span>{product.stock} in stock</span>
                  <button className="ghost-button" type="button">
                    Add to cart
                  </button>
                </div>
              </div>
            </article>
          ))}
        </section>
      )}
    </main>
  );
}
