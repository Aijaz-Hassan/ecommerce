import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../api/client";
import { useCart } from "../context/CartContext";
import { fallbackProducts } from "../data/fallbackProducts";

export default function ProductDetailsPage() {
  const { id } = useParams();
  const { addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let ignore = false;

    async function loadProduct() {
      setStatus("loading");
      try {
        const response = await api.get(`/products/${id}`);
        if (!ignore) {
          setProduct(response.data);
          setStatus("ready");
        }
      } catch (error) {
        if (!ignore) {
          const localProduct = fallbackProducts.find((item) => String(item.id) === String(id));
          setProduct(localProduct || null);
          setStatus("ready");
        }
      }
    }

    loadProduct();
    return () => {
      ignore = true;
    };
  }, [id]);

  if (status === "loading") {
    return <main className="page"><div className="loading-panel">Loading product details...</div></main>;
  }

  if (!product) {
    return (
      <main className="page">
        <div className="loading-panel">Product not found.</div>
      </main>
    );
  }

  return (
    <main className="page">
      <section className="product-page-layout">
        <div className="product-page-image-wrap">
          <img className="product-page-image" src={product.imageUrl} alt={product.name} />
        </div>
        <div className="product-page-copy">
          <p className="eyebrow">{product.category}</p>
          <h1>{product.name}</h1>
          <strong className="product-page-price">${Number(product.price).toFixed(2)}</strong>
          <div className="product-description-box">
            <strong>Description</strong>
            <p>{product.description}</p>
          </div>
          <p className="section-note">{product.stock} item(s) currently in stock.</p>
          <div className="hero-actions">
            <button className="solid-button" type="button" onClick={() => addToCart(product)}>
              Add to cart
            </button>
            <Link className="ghost-link" to="/cart">
              View cart
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
