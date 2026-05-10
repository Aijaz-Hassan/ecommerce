import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import { getProductOptions } from "../data/productOptions";
import { useCart } from "../context/CartContext";

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existingScript = document.querySelector('script[data-razorpay="true"]');
    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(true), { once: true });
      existingScript.addEventListener("error", () => resolve(false), { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.dataset.razorpay = "true";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export default function CartPage() {
  const {
    cartItems,
    cartCount,
    cartTotal,
    increaseQuantity,
    decreaseQuantity,
    removeFromCart,
    clearCart,
    refreshCart,
    updateCartItem
  } = useCart();
  const [message, setMessage] = useState("");
  const [busyItemId, setBusyItemId] = useState(null);
  const [clearing, setClearing] = useState(false);
  const [buying, setBuying] = useState(false);
  const [showBillDetails, setShowBillDetails] = useState(false);

  const subtotal = cartItems.reduce((total, item) => total + Number(item.price) * item.quantity, 0);
  const taxAmount = Number((subtotal * 0.18).toFixed(2));
  const shippingAmount = subtotal >= 500 || subtotal === 0 ? 0 : 49;
  const grandTotal = Number((subtotal + taxAmount + shippingAmount).toFixed(2));

  useEffect(() => {
    refreshCart().catch((error) => {
      setMessage(error.response?.data?.message || "Unable to load cart.");
    });
  }, []);

  const runCartAction = async (cartItemId, action, successMessage) => {
    setBusyItemId(cartItemId);
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

  const handleVariantChange = async (cartItemId, updates) => {
    setBusyItemId(cartItemId);
    setMessage("");
    try {
      await updateCartItem(cartItemId, updates);
      setMessage("Cart item updated successfully.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to update item preferences.");
    } finally {
      setBusyItemId(null);
    }
  };

  const handleBuyNow = async () => {
    setBuying(true);
    setMessage("");

    try {
      const { data } = await api.post("/cart/checkout/session");

      if (data.provider === "razorpay") {
        const loaded = await loadRazorpayScript();
        if (!loaded) {
          throw new Error("Unable to load Razorpay checkout.");
        }

        const razorpay = new window.Razorpay({
          key: data.keyId,
          amount: data.amount,
          currency: data.currency,
          name: data.merchantName,
          description: data.description,
          order_id: data.orderId,
          prefill: {
            name: data.customerName,
            email: data.customerEmail
          },
          theme: {
            color: "#e85d3f"
          },
          handler: async (response) => {
            await api.post("/cart/checkout/confirm", {
              provider: "razorpay",
              orderId: response.razorpay_order_id,
              paymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature
            });
            await refreshCart();
            setMessage("Payment successful and cart cleared.");
          }
        });

        razorpay.on("payment.failed", () => {
          setMessage("Payment was not completed.");
        });

        razorpay.open();
      } else {
        const accepted = window.confirm(
          `Demo payment for INR ${(data.amount / 100).toFixed(2)}. Click OK to simulate a successful payment.`
        );

        if (!accepted) {
          setMessage("Demo payment cancelled.");
          return;
        }

        await api.post("/cart/checkout/confirm", {
          provider: "demo",
          orderId: data.orderId,
          paymentId: `demo-payment-${Date.now()}`
        });
        await refreshCart();
        setMessage("Demo payment successful and cart cleared.");
      }
    } catch (error) {
      setMessage(error.response?.data?.message || error.message || "Unable to start checkout.");
    } finally {
      setBuying(false);
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
                <article className="cart-item-row" key={item.cartItemId}>
                  <img className="cart-item-image" src={item.imageUrl} alt={item.name} />
                  <div className="cart-item-info">
                    <strong>{item.name}</strong>
                    <p>{item.category}</p>
                    <span>${Number(item.price).toFixed(2)}</span>
                    <div className="cart-variant-stack">
                      <div className="cart-variant-grid">
                        <label className="filter-field compact-field">
                          <span>Color</span>
                          <select
                            value={item.selectedColor || ""}
                            onChange={(event) => handleVariantChange(item.cartItemId, { selectedColor: event.target.value })}
                            disabled={busyItemId === item.cartItemId}
                          >
                            {getProductOptions(item).colors.map((color) => (
                              <option key={color} value={color}>
                                {color}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label className="filter-field compact-field">
                          <span>Size</span>
                          <select
                            value={item.selectedSize || ""}
                            onChange={(event) => handleVariantChange(item.cartItemId, { selectedSize: event.target.value })}
                            disabled={busyItemId === item.cartItemId}
                          >
                            {getProductOptions(item).sizes.map((size) => (
                              <option key={size} value={size}>
                                {size}
                              </option>
                            ))}
                          </select>
                        </label>
                      </div>
                      <label className="filter-field compact-field">
                        <span>Customization note</span>
                        <textarea
                          rows="2"
                          value={item.customizationNote || ""}
                          onChange={(event) => handleVariantChange(item.cartItemId, { customizationNote: event.target.value })}
                          disabled={busyItemId === item.cartItemId}
                          placeholder="Add finish, engraving, gift-wrap or special preference"
                        />
                      </label>
                    </div>
                  </div>

                  <div className="cart-qty-controls">
                    <button
                      className="ghost-button"
                      type="button"
                      onClick={() => runCartAction(item.cartItemId, () => decreaseQuantity(item.cartItemId))}
                      disabled={busyItemId === item.cartItemId}
                    >
                      -
                    </button>
                    <strong>{item.quantity}</strong>
                    <button
                      className="ghost-button"
                      type="button"
                      onClick={() => runCartAction(item.cartItemId, () => increaseQuantity(item.cartItemId))}
                      disabled={busyItemId === item.cartItemId}
                    >
                      +
                    </button>
                  </div>
                  <button
                    className="danger-button"
                    type="button"
                    onClick={() => runCartAction(item.cartItemId, () => removeFromCart(item.cartItemId), "Item removed from cart.")}
                    disabled={busyItemId === item.cartItemId}
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
              <strong>${grandTotal.toFixed(2)}</strong>
            </div>
            <button className="ghost-button full-width-link" type="button" onClick={() => setShowBillDetails((current) => !current)}>
              {showBillDetails ? "Hide bill details" : "Bill details"}
            </button>
            {showBillDetails && (
              <div className="bill-breakdown">
                <div className="summary-line">
                  <span>Subtotal</span>
                  <strong>${subtotal.toFixed(2)}</strong>
                </div>
                <div className="summary-line">
                  <span>Tax (18%)</span>
                  <strong>${taxAmount.toFixed(2)}</strong>
                </div>
                <div className="summary-line">
                  <span>Shipping</span>
                  <strong>${shippingAmount.toFixed(2)}</strong>
                </div>
                <div className="summary-line">
                  <span>Payable amount</span>
                  <strong>${grandTotal.toFixed(2)}</strong>
                </div>
              </div>
            )}
            {message && <p className={message.includes("successful") ? "success-text" : "error-text"}>{message}</p>}
            <button className="solid-button full-width-link" type="button" disabled={!cartItems.length || buying} onClick={handleBuyNow}>
              {buying ? "Starting payment..." : "Buy now"}
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
