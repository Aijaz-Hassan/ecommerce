import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { fallbackProducts } from "../data/fallbackProducts";
import { isAdminRole } from "../utils/roles";

export default function ProductsPage() {
  const { addToCart } = useCart();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");
  const [searchText, setSearchText] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [filteredProducts, setFilteredProducts] = useState([]);
  const isAdmin = isAdminRole(user?.role);

  useEffect(() => {
    let ignore = false;

    async function fetchProducts() {
      try {
        const response = await api.get("/products");
        if (!ignore) {
          const incoming = response.data || [];
          setProducts(incoming);
          setFilteredProducts(incoming);
          setStatus("ready");
          setMessage(incoming.length ? "" : "No products are available yet.");
        }
      } catch (error) {
        if (!ignore) {
          setProducts(fallbackProducts);
          setFilteredProducts(fallbackProducts);
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

  const categories = ["All", ...new Set(products.map((product) => product.category))];

  const applyFilters = async () => {
    try {
      const response = await api.get("/products", {
        params: {
          search: searchText.trim() || undefined,
          category: selectedCategory === "All" ? undefined : selectedCategory
        }
      });
      setFilteredProducts(response.data);
      setMessage(response.data.length ? "" : "No products matched your search and filter.");
    } catch (error) {
      const normalizedSearch = searchText.trim().toLowerCase();
      const nextProducts = products.filter((product) => {
        const matchesSearch =
          !normalizedSearch ||
          product.name.toLowerCase().includes(normalizedSearch) ||
          product.description.toLowerCase().includes(normalizedSearch) ||
          product.category.toLowerCase().includes(normalizedSearch);
        const matchesCategory = selectedCategory === "All" || product.category === selectedCategory;
        return matchesSearch && matchesCategory;
      });
      setFilteredProducts(nextProducts);
      setMessage(nextProducts.length ? "Showing locally filtered preview products." : "No products matched your search and filter.");
    }
  };

  const clearFilters = () => {
    setSearchText("");
    setSelectedCategory("All");
    setFilteredProducts(products);
    setMessage("");
  };

  const handleAddToCart = async (product) => {
    if (!Number.isFinite(Number(product.id))) {
      window.alert("Preview products cannot be added to cart until the backend product list is available.");
      return;
    }

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
      <section className="section-heading">
        <div>
          <p className="eyebrow">Product gallery</p>
          <h1>Dynamic product listing built for browsing, search, and filtering.</h1>
        </div>
        {message && <p className="section-note">{message}</p>}
      </section>

      {status === "loading" ? (
        <div className="loading-panel">Loading products...</div>
      ) : (
        <>
          <section className="filter-bar">
            <label className="filter-field">
              <span>Search</span>
              <input
                type="text"
                placeholder="Search by name, category, or description"
                value={searchText}
                onChange={(event) => setSearchText(event.target.value)}
              />
            </label>

            <label className="filter-field">
              <span>Category</span>
              <select value={selectedCategory} onChange={(event) => setSelectedCategory(event.target.value)}>
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </label>

            <div className="filter-actions">
              <button className="solid-button" type="button" onClick={applyFilters}>
                Search & Filter
              </button>
              <button className="ghost-button" type="button" onClick={clearFilters}>
                Clear
              </button>
            </div>
          </section>

          <section className="products-grid">
            {filteredProducts.length === 0 ? (
              <div className="loading-panel">No products matched your current search.</div>
            ) : (
              filteredProducts.map((product) => (
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
                    <div className="product-description-box">
                      <strong>Description</strong>
                      <p>{product.description}</p>
                    </div>
                    <div className="product-meta">
                      <span>{product.stock} in stock</span>
                      <div className="product-actions">
                        <button className="ghost-button" type="button" onClick={() => navigate(`/products/${product.id}`)}>
                          View details
                        </button>
                        {!isAdmin && (
                          <button className="ghost-button" type="button" onClick={() => handleAddToCart(product)}>
                            Add to cart
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </article>
              ))
            )}
          </section>
        </>
      )}
    </main>
  );
}
