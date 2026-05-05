import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import api from "../api/client";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { fallbackProducts } from "../data/fallbackProducts";
import { isAdminRole } from "../utils/roles";

export default function ProductDetailsPage() {
  const { id } = useParams();
  const { addToCart } = useCart();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [status, setStatus] = useState("loading");
  const isAdmin = isAdminRole(user?.role);

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

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      window.alert("Please login as a customer to use the cart.");
      navigate("/login");
      return;
    }

    if (isAdmin) {
      window.alert("Admin accounts cannot add items to the cart.");
      return;
    }

    try {
      await addToCart(product);
      window.alert(`${product.name} added to cart.`);
    } catch (error) {
      window.alert(error.response?.data?.message || error.message || "Unable to add product to cart.");
    }
  };

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
            {!isAdmin && (
              <>
                <button className="solid-button" type="button" onClick={handleAddToCart}>
                  Add to cart
                </button>
                <Link className="ghost-link" to={isAuthenticated ? "/cart" : "/login"}>
                  View cart
                </Link>
              </>
            )}
          </div>
        </div>
      </section>
    </main>
  );
}
