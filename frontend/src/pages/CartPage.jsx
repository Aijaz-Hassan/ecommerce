import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useEffect, useState } from "react";

export default function CartPage() {
  const { cartItems, cartCount, cartTotal, increaseQuantity, decreaseQuantity, removeFromCart, clearCart, refreshCart } = useCart();
  const [message, setMessage] = useState("");
  const [busyItemId, setBusyItemId] = useState(null);
  const [clearing, setClearing] = useState(false);

  useEffect(() => {
    refreshCart().catch((error) => {
      setMessage(error.response?.data?.message || "Unable to load cart.");
    });
  }, []);

  const runCartAction = async (productId, action, successMessage) => {
    setBusyItemId(productId);
    setMessage("");
    try {
      await action();
      if (successMessage) {
        setMessage(successMessage);
      }
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to update cart.");
    } finally {
      setBusyItemId(null);
    }
  };

  const handleClearCart = async () => {
    setClearing(true);
    setMessage("");
    try {
      await clearCart();
      setMessage("Cart cleared successfully.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to clear cart.");
    } finally {
      setClearing(false);
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
              <button className="ghost-button" type="button" onClick={handleClearCart} disabled={clearing}>
                {clearing ? "Clearing..." : "Clear cart"}
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
                    <button
                      className="ghost-button"
                      type="button"
                      onClick={() => runCartAction(item.id, () => decreaseQuantity(item.id))}
                      disabled={busyItemId === item.id}
                    >
                      -
                    </button>
                    <strong>{item.quantity}</strong>
                    <button
                      className="ghost-button"
                      type="button"
                      onClick={() => runCartAction(item.id, () => increaseQuantity(item.id))}
                      disabled={busyItemId === item.id}
                    >
                      +
                    </button>
                  </div>
                  <button
                    className="danger-button"
                    type="button"
                    onClick={() => runCartAction(item.id, () => removeFromCart(item.id), "Item removed from cart.")}
                    disabled={busyItemId === item.id}
                  >
                    Remove
                  </button>
                </article>
              ))
            )}
          </div>
        </div>

        <aside className="cart-aside">
          <div className="admin-panel">
            <h2>Cart summary</h2>
            <div className="summary-line">
              <span>Items</span>
              <strong>{cartCount}</strong>
            </div>
            <div className="summary-line">
              <span>Total</span>
              <strong>${cartTotal.toFixed(2)}</strong>
            </div>
            {message && <p className={message.includes("successfully") ? "success-text" : "error-text"}>{message}</p>}
            <Link className="solid-link full-width-link" to="/products">
              Continue shopping
            </Link>
          </div>
        </aside>
      </section>
    </main>
  );
}
