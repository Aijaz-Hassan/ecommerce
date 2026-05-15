import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import { useCart } from "../context/CartContext";

const addressStorageKey = "store-checkout-address";

const emptyAddress = {
  recipientName: "",
  phoneNumber: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "India"
};

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
    increaseQuantity,
    decreaseQuantity,
    removeFromCart,
    clearCart,
    refreshCart
  } = useCart();
  const [message, setMessage] = useState("");
  const [busyItemId, setBusyItemId] = useState(null);
  const [clearing, setClearing] = useState(false);
  const [buying, setBuying] = useState(false);
  const [showBillDetails, setShowBillDetails] = useState(false);
  const [address, setAddress] = useState(() => {
    try {
      const saved = window.localStorage.getItem(addressStorageKey);
      return saved ? { ...emptyAddress, ...JSON.parse(saved) } : emptyAddress;
    } catch {
      return emptyAddress;
    }
  });

  const subtotal = cartItems.reduce((total, item) => total + Number(item.price) * item.quantity, 0);
  const taxAmount = Number((subtotal * 0.18).toFixed(2));
  const shippingAmount = subtotal >= 500 || subtotal === 0 ? 0 : 49;
  const grandTotal = Number((subtotal + taxAmount + shippingAmount).toFixed(2));
  const mapQuery = encodeURIComponent(
    [address.addressLine1, address.addressLine2, address.city, address.state, address.postalCode, address.country]
      .filter(Boolean)
      .join(", ")
  );

  useEffect(() => {
    refreshCart().catch((error) => {
      setMessage(error.response?.data?.message || "Unable to load cart.");
    });
  }, []);

  useEffect(() => {
    window.localStorage.setItem(addressStorageKey, JSON.stringify(address));
  }, [address]);

  const handleAddressChange = (event) => {
    const { name, value } = event.target;
    setAddress((current) => ({ ...current, [name]: value }));
  };

  const validateAddress = () => {
    const requiredFields = [
      ["recipientName", "Please add the recipient name."],
      ["phoneNumber", "Please add a phone number."],
      ["addressLine1", "Please add address line 1."],
      ["city", "Please add the delivery city."],
      ["state", "Please add the delivery state."],
      ["postalCode", "Please add the postal code."],
      ["country", "Please add the country."]
    ];

    const missing = requiredFields.find(([field]) => !address[field]?.trim());
    return missing ? missing[1] : "";
  };

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

  const handleBuyNow = async () => {
    setBuying(true);
    setMessage("");

    try {
      const validationMessage = validateAddress();
      if (validationMessage) {
        setMessage(validationMessage);
        return;
      }

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
            name: address.recipientName || data.customerName,
            email: data.customerEmail,
            contact: address.phoneNumber
          },
          theme: {
            color: "#e85d3f"
          },
          handler: async (response) => {
            await api.post("/orders/checkout", {
              paymentProvider: "razorpay",
              paymentReference: response.razorpay_payment_id,
              ...address
            });
            await refreshCart();
            setMessage("Payment successful. Your order is now available in My Orders.");
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

        await api.post("/orders/checkout", {
          paymentProvider: "demo",
          paymentReference: `demo-payment-${Date.now()}`,
          ...address
        });
        await refreshCart();
        setMessage("Demo payment successful. Your order is now available in My Orders.");
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

          <div className="admin-panel address-panel">
            <h2>Delivery address</h2>
            <div className="address-form-grid">
              <label className="filter-field">
                <span>Recipient name</span>
                <input name="recipientName" value={address.recipientName} onChange={handleAddressChange} placeholder="Full name" />
              </label>
              <label className="filter-field">
                <span>Phone number</span>
                <input name="phoneNumber" value={address.phoneNumber} onChange={handleAddressChange} placeholder="10-digit number" />
              </label>
              <label className="filter-field full-span">
                <span>Address line 1</span>
                <input name="addressLine1" value={address.addressLine1} onChange={handleAddressChange} placeholder="House number and street" />
              </label>
              <label className="filter-field full-span">
                <span>Address line 2</span>
                <input name="addressLine2" value={address.addressLine2} onChange={handleAddressChange} placeholder="Apartment, landmark, area" />
              </label>
              <label className="filter-field">
                <span>City</span>
                <input name="city" value={address.city} onChange={handleAddressChange} placeholder="City" />
              </label>
              <label className="filter-field">
                <span>State</span>
                <input name="state" value={address.state} onChange={handleAddressChange} placeholder="State" />
              </label>
              <label className="filter-field">
                <span>Postal code</span>
                <input name="postalCode" value={address.postalCode} onChange={handleAddressChange} placeholder="Postal code" />
              </label>
              <label className="filter-field">
                <span>Country</span>
                <input name="country" value={address.country} onChange={handleAddressChange} placeholder="Country" />
              </label>
            </div>

            <div className="map-frame-wrap compact-map">
              <iframe
                title="Checkout address map"
                src={`https://www.google.com/maps?q=${mapQuery}&z=15&output=embed`}
                loading="lazy"
                referrerPolicy="no-referrer-when-downgrade"
              />
            </div>
          </div>
        </aside>
      </section>
    </main>
  );
}
