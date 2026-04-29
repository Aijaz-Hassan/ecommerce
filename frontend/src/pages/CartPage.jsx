import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import api from "../api/client";
import { useState } from "react";

export default function CartPage() {
  const { cartItems, cartCount, cartTotal, increaseQuantity, decreaseQuantity, removeFromCart, clearCart } = useCart();
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleCheckout = async () => {
    if (!cartItems.length) {
      return;
    }

    setSubmitting(true);
    setMessage("");

    try {
      await api.post("/orders/checkout", {
        items: cartItems.map((item) => ({
          productId: Number(item.id),
          quantity: item.quantity
        }))
      });
      clearCart();
      setMessage("Purchase completed successfully.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to complete purchase.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Your cart</p>
          <h1>Review everything you have added before checkout.</h1>
        </div>
      </section>

      <section className="cart-layout">
        <div className="cart-summary-panel">
          <div className="admin-panel-header">
            <h2>Cart items</h2>
            {cartItems.length > 0 && (
              <button className="ghost-button" type="button" onClick={clearCart}>
                Clear cart
              </button>
            )}
          </div>

          <div className="admin-table">
            {cartItems.length === 0 ? (
              <div className="admin-row">
                <div>
                  <strong>Your cart is empty</strong>
                  <p>Browse the catalog and add products to see them here.</p>
                </div>
                <Link className="solid-link" to="/products">
                  Go to products
                </Link>
              </div>
            ) : (
              cartItems.map((item) => (
                <article className="cart-item-row" key={item.id}>
                  <img className="cart-item-image" src={item.imageUrl} alt={item.name} />
                  <div className="cart-item-info">
                    <strong>{item.name}</strong>
                    <p>{item.category}</p>
                    <span>${Number(item.price).toFixed(2)}</span>
                  </div>
                  <div className="cart-qty-controls">
                    <button className="ghost-button" type="button" onClick={() => decreaseQuantity(item.id)}>
                      -
                    </button>
                    <strong>{item.quantity}</strong>
                    <button className="ghost-button" type="button" onClick={() => increaseQuantity(item.id)}>
                      +
                    </button>
                  </div>
                  <button className="danger-button" type="button" onClick={() => removeFromCart(item.id)}>
                    Remove
                  </button>
                </article>
              ))
            )}
          </div>
        </div>

        <aside className="cart-aside">
          <div className="admin-panel">
            <h2>Order summary</h2>
            <div className="summary-line">
              <span>Items</span>
              <strong>{cartCount}</strong>
            </div>
            <div className="summary-line">
              <span>Total</span>
              <strong>${cartTotal.toFixed(2)}</strong>
            </div>
            {message && <p className={message.includes("successfully") ? "success-text" : "error-text"}>{message}</p>}
            <button
              className="solid-button full-width-link"
              type="button"
              onClick={handleCheckout}
              disabled={!cartItems.length || submitting}
            >
              {submitting ? "Processing..." : "Buy now"}
            </button>
            <Link className="solid-link full-width-link" to="/products">
              Continue shopping
            </Link>
          </div>
        </aside>
      </section>
    </main>
  );
}
