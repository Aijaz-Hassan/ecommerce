import { useEffect, useMemo, useState } from "react";
import { Heart, PackageOpen, ShoppingBag, Tag, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { formatCurrency } from "../utils/currency";

const legacyAddressStorageKey = "store-checkout-address";
const legacyAddressMigrationPrefix = "lumenlane_address_migrated";
const savedLaterKey = "lumenlane_saved_later";

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

const addressFields = ["recipientName", "phoneNumber", "addressLine1", "addressLine2", "city", "state", "postalCode", "country"];

const getLegacyMigrationKey = (user) => `${legacyAddressMigrationPrefix}_${user?.id || user?.email || "anonymous"}`;

const toCheckoutAddress = (source = {}) =>
  addressFields.reduce((result, field) => ({ ...result, [field]: source[field] || emptyAddress[field] }), {});

const hasAddressContent = (address) =>
  ["addressLine1", "city", "state", "postalCode", "country"].some((field) => String(address?.[field] || "").trim());

const hasRequiredAddressFields = (address) =>
  ["recipientName", "phoneNumber", "addressLine1", "city", "state", "postalCode", "country"].every((field) => String(address?.[field] || "").trim());

const readLegacyAddress = () => {
  try {
    const saved = window.localStorage.getItem(legacyAddressStorageKey);
    if (!saved) {
      return null;
    }
    const address = toCheckoutAddress(JSON.parse(saved));
    return hasAddressContent(address) ? { label: "Saved checkout address", ...address } : null;
  } catch {
    return null;
  }
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

function readSavedLater() {
  try {
    const saved = window.localStorage.getItem(savedLaterKey);
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
}

export default function CartPage() {
  const { user } = useAuth();
  const { cartItems, cartCount, cartLoading, increaseQuantity, decreaseQuantity, removeFromCart, clearCart, refreshCart } = useCart();
  const [toast, setToast] = useState(null);
  const [busyItemId, setBusyItemId] = useState(null);
  const [clearing, setClearing] = useState(false);
  const [buying, setBuying] = useState(false);
  const [coupon, setCoupon] = useState("");
  const [appliedCoupon, setAppliedCoupon] = useState("");
  const [savedLater, setSavedLater] = useState(readSavedLater);
  const [savedAddresses, setSavedAddresses] = useState([]);
  const [addressesLoading, setAddressesLoading] = useState(true);
  const [savingAddress, setSavingAddress] = useState(false);
  const [selectedAddressId, setSelectedAddressId] = useState("");
  const [editingAddressId, setEditingAddressId] = useState("");
  const [address, setAddress] = useState(emptyAddress);

  const subtotal = useMemo(() => cartItems.reduce((total, item) => total + Number(item.price) * item.quantity, 0), [cartItems]);
  const discount = appliedCoupon === "LUMEN20" ? Number((subtotal * 0.2).toFixed(2)) : appliedCoupon === "FREESHIP" ? 0 : 0;
  const shippingAmount = subtotal >= 500 || subtotal === 0 || appliedCoupon === "FREESHIP" ? 0 : 49;
  const taxAmount = Number(((subtotal - discount) * 0.18).toFixed(2));
  const grandTotal = Number((subtotal - discount + taxAmount + shippingAmount).toFixed(2));
  const mapQuery = encodeURIComponent(
    [address.addressLine1, address.addressLine2, address.city, address.state, address.postalCode, address.country].filter(Boolean).join(", ")
  );

  useEffect(() => {
    refreshCart().catch(() => {
      showToast("Unable to load cart.", "error");
    });
  }, []);

  useEffect(() => {
    if (!user) {
      return;
    }
    let ignore = false;

    async function loadAddresses() {
      setAddressesLoading(true);
      try {
        const response = await api.get("/addresses");
        let nextAddresses = response.data || [];
        const migrationKey = getLegacyMigrationKey(user);
        const legacyAddress = !window.localStorage.getItem(migrationKey) ? readLegacyAddress() : null;

        if (nextAddresses.length === 0 && legacyAddress && hasRequiredAddressFields(legacyAddress)) {
          const migrationResponse = await api.post("/addresses", legacyAddress);
          nextAddresses = [migrationResponse.data];
        }
        if (!legacyAddress || nextAddresses.length > 0) {
          window.localStorage.setItem(migrationKey, "true");
          window.localStorage.removeItem(legacyAddressStorageKey);
        }

        if (!ignore) {
          const firstAddress = nextAddresses[0];
          setSavedAddresses(nextAddresses);
          setSelectedAddressId(firstAddress?.id || "");
          setEditingAddressId("");
          setAddress(firstAddress ? toCheckoutAddress(firstAddress) : legacyAddress || toCheckoutAddress({ recipientName: user.fullName, phoneNumber: user.phoneNumber }));
        }
      } catch (error) {
        if (!ignore) {
          showToast(error.response?.data?.message || "Unable to load saved addresses.", "error");
        }
      } finally {
        if (!ignore) {
          setAddressesLoading(false);
        }
      }
    }

    loadAddresses();
    return () => {
      ignore = true;
    };
  }, [user?.id, user?.email]);

  useEffect(() => {
    window.localStorage.setItem(savedLaterKey, JSON.stringify(savedLater));
  }, [savedLater]);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    window.setTimeout(() => setToast(null), 2600);
  };

  const handleAddressChange = (event) => {
    const { name, value } = event.target;
    if (selectedAddressId && !editingAddressId) {
      setSelectedAddressId("");
    }
    setAddress((current) => ({ ...current, [name]: value }));
  };

  const validateAddress = (candidate = address) => {
    const requiredFields = [
      ["recipientName", "Please add the recipient name."],
      ["phoneNumber", "Please add a phone number."],
      ["addressLine1", "Please add address line 1."],
      ["city", "Please add the delivery city."],
      ["state", "Please add the delivery state."],
      ["postalCode", "Please add the postal code."],
      ["country", "Please add the country."]
    ];

    const missing = requiredFields.find(([field]) => !candidate[field]?.trim());
    return missing ? missing[1] : "";
  };

  const useSavedAddress = (savedAddress) => {
    if (!savedAddress) {
      startNewAddress();
      return;
    }
    setSelectedAddressId(savedAddress.id);
    setEditingAddressId("");
    setAddress(toCheckoutAddress(savedAddress));
    showToast(`${savedAddress.label} selected for delivery.`);
  };

  const startNewAddress = () => {
    setSelectedAddressId("");
    setEditingAddressId("");
    setAddress(toCheckoutAddress({ recipientName: user?.fullName, phoneNumber: user?.phoneNumber }));
  };

  const startAddressEdit = (savedAddress) => {
    setSelectedAddressId(savedAddress.id);
    setEditingAddressId(savedAddress.id);
    setAddress(toCheckoutAddress(savedAddress));
  };

  const saveAddress = async () => {
    const validationMessage = validateAddress();
    if (validationMessage) {
      showToast(validationMessage, "error");
      return;
    }

    const existingIndex = savedAddresses.findIndex((entry) => entry.id === editingAddressId);
    const nextAddressNumber = savedAddresses.reduce((highest, entry) => {
      const match = /^Address (\d+)$/.exec(entry.label || "");
      return match ? Math.max(highest, Number(match[1])) : highest;
    }, 0) + 1;
    const payload = {
      label: existingIndex >= 0 ? savedAddresses[existingIndex].label : `Address ${nextAddressNumber}`,
      ...toCheckoutAddress(address)
    };

    setSavingAddress(true);
    try {
      const response = editingAddressId
        ? await api.put(`/addresses/${editingAddressId}`, payload)
        : await api.post("/addresses", payload);
      const savedAddress = response.data;
      setSavedAddresses((current) =>
        existingIndex >= 0 ? current.map((entry) => (entry.id === savedAddress.id ? savedAddress : entry)) : [...current, savedAddress]
      );
      setSelectedAddressId(savedAddress.id);
      setEditingAddressId("");
      setAddress(toCheckoutAddress(savedAddress));
      showToast(existingIndex >= 0 ? "Delivery address updated." : "Delivery address saved.");
    } catch (error) {
      showToast(error.response?.data?.message || "Unable to save delivery address.", "error");
    } finally {
      setSavingAddress(false);
    }
  };

  const deleteAddress = async (savedAddress) => {
    if (!window.confirm(`Delete ${savedAddress.label}?`)) {
      return;
    }

    try {
      await api.delete(`/addresses/${savedAddress.id}`);
      const remainingAddresses = savedAddresses.filter((entry) => entry.id !== savedAddress.id);
      setSavedAddresses(remainingAddresses);
      if (selectedAddressId === savedAddress.id) {
        const nextAddress = remainingAddresses[0];
        setSelectedAddressId(nextAddress?.id || "");
        setEditingAddressId("");
        setAddress(nextAddress ? toCheckoutAddress(nextAddress) : toCheckoutAddress({ recipientName: user?.fullName, phoneNumber: user?.phoneNumber }));
      }
      showToast("Delivery address deleted.");
    } catch (error) {
      showToast(error.response?.data?.message || "Unable to delete delivery address.", "error");
    }
  };

  const runCartAction = async (cartItemId, action, successMessage) => {
    setBusyItemId(cartItemId);
    try {
      await action();
      showToast(successMessage || "Cart updated.");
    } catch (error) {
      showToast(error.response?.data?.message || "Unable to update cart.", "error");
    } finally {
      setBusyItemId(null);
    }
  };

  const handleClearCart = async () => {
    setClearing(true);
    try {
      await clearCart();
      showToast("Cart cleared successfully.");
    } catch (error) {
      showToast(error.response?.data?.message || "Unable to clear cart.", "error");
    } finally {
      setClearing(false);
    }
  };

  const saveForLater = async (item) => {
    setSavedLater((current) => [item, ...current.filter((entry) => entry.cartItemId !== item.cartItemId)]);
    await runCartAction(item.cartItemId, () => removeFromCart(item.cartItemId), "Item saved for later.");
  };

  const applyCoupon = () => {
    const normalized = coupon.trim().toUpperCase();
    if (!["LUMEN20", "FREESHIP"].includes(normalized)) {
      showToast("Use LUMEN20 or FREESHIP for demo discounts.", "error");
      return;
    }
    setAppliedCoupon(normalized);
    showToast(`${normalized} applied.`);
  };

  const handleBuyNow = async () => {
    setBuying(true);

    try {
      const validationMessage = validateAddress();
      if (validationMessage) {
        showToast(validationMessage, "error");
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
            showToast("Payment successful. Your order is now available in My Orders.");
          }
        });

        razorpay.on("payment.failed", () => {
          showToast("Payment was not completed.", "error");
        });

        razorpay.open();
      } else {
        const accepted = window.confirm(`Demo payment for ${formatCurrency(data.amount / 100)}. Click OK to simulate a successful payment.`);

        if (!accepted) {
          showToast("Demo payment cancelled.", "error");
          return;
        }

        await api.post("/orders/checkout", {
          paymentProvider: "demo",
          paymentReference: `demo-payment-${Date.now()}`,
          ...address
        });
        await refreshCart();
        showToast("Demo payment successful. Your order is now available in My Orders.");
      }
    } catch (error) {
      showToast(error.response?.data?.message || error.message || "Unable to start checkout.", "error");
    } finally {
      setBuying(false);
    }
  };

  if (cartLoading && cartItems.length === 0) {
    return (
      <main className="page premium-cart-page">
        <section className="empty-cart-state">
          <div className="empty-cart-illustration">
            <ShoppingBag size={76} />
          </div>
          <p className="eyebrow">Loading cart</p>
          <h1>Getting your personal cart.</h1>
          <p>Your cart is loaded securely from your authenticated account.</p>
        </section>
      </main>
    );
  }

  if (cartItems.length === 0) {
    return (
      <main className="page premium-cart-page">
        {toast && <div className={`floating-toast ${toast.type}`}>{toast.message}</div>}
        <section className="empty-cart-state">
          <div className="empty-cart-illustration">
            <PackageOpen size={76} />
          </div>
          <p className="eyebrow">Your cart is empty</p>
          <h1>Start building a cart worth checking out.</h1>
          <p>Explore premium products, save favorites, and come back here when you are ready to buy.</p>
          <Link className="solid-link" to="/products">Continue Shopping</Link>
        </section>
        {savedLater.length > 0 && (
          <section className="saved-later-section">
            <h2>Saved for later</h2>
            <div className="saved-later-grid">
              {savedLater.map((item) => (
                <article key={item.cartItemId}>
                  <img src={item.imageUrl} alt={item.name} />
                  <strong>{item.name}</strong>
                  <span>{formatCurrency(item.price)}</span>
                </article>
              ))}
            </div>
          </section>
        )}
      </main>
    );
  }

  return (
    <main className="page premium-cart-page">
      {toast && <div className={`floating-toast ${toast.type}`}>{toast.message}</div>}

      <section className="cart-hero-bar">
        <div>
          <p className="eyebrow">Shopping cart</p>
          <h1>Review your items and checkout securely.</h1>
          <p>{cartCount} item(s) ready for delivery.</p>
        </div>
        <button className="ghost-button" type="button" onClick={handleClearCart} disabled={clearing}>
          <Trash2 size={17} />
          {clearing ? "Clearing..." : "Remove All"}
        </button>
      </section>

      <section className="premium-cart-layout">
        <div className="cart-items-panel">
          {cartItems.map((item) => {
            const itemTotal = Number(item.price) * item.quantity;
            return (
              <article className="premium-cart-item swipe-card" key={item.cartItemId}>
                <img src={item.imageUrl} alt={item.name} loading="lazy" />
                <div className="premium-cart-copy">
                  <span>{item.brand || item.category || "Lumen Lane"}</span>
                  <h2>{item.name}</h2>
                  <p>{item.category}</p>
                  <strong className="stock-text">In stock</strong>
                  <div className="cart-item-actions">
                    <button type="button" onClick={() => saveForLater(item)}>
                      <Heart size={16} />
                      Save for later
                    </button>
                    <button type="button" onClick={() => runCartAction(item.cartItemId, () => removeFromCart(item.cartItemId), "Item removed from cart.")}>
                      <Trash2 size={16} />
                      Remove
                    </button>
                  </div>
                </div>
                <div className="cart-quantity-panel">
                  <strong>{formatCurrency(item.price)}</strong>
                  <div className="quantity-selector">
                    <button type="button" onClick={() => runCartAction(item.cartItemId, () => decreaseQuantity(item.cartItemId), "Quantity updated.")} disabled={busyItemId === item.cartItemId}>-</button>
                    <span>{item.quantity}</span>
                    <button type="button" onClick={() => runCartAction(item.cartItemId, () => increaseQuantity(item.cartItemId), "Quantity updated.")} disabled={busyItemId === item.cartItemId}>+</button>
                  </div>
                  <em>{formatCurrency(itemTotal)}</em>
                </div>
              </article>
            );
          })}

          {savedLater.length > 0 && (
            <section className="saved-later-section inline-saved">
              <h2>Saved for later</h2>
              <div className="saved-later-grid">
                {savedLater.map((item) => (
                  <article key={item.cartItemId}>
                    <img src={item.imageUrl} alt={item.name} />
                    <strong>{item.name}</strong>
                    <span>{formatCurrency(item.price)}</span>
                  </article>
                ))}
              </div>
            </section>
          )}
        </div>

        <aside className="checkout-column">
          <section className="order-summary-card">
            <div className="summary-title-row">
              <ShoppingBag size={20} />
              <h2>Order Summary</h2>
            </div>
            <div className="coupon-box">
              <Tag size={17} />
              <input value={coupon} onChange={(event) => setCoupon(event.target.value)} placeholder="LUMEN20 or FREESHIP" />
              <button type="button" onClick={applyCoupon}>Apply</button>
            </div>
            <div className="summary-line">
              <span>Subtotal</span>
              <strong>{formatCurrency(subtotal)}</strong>
            </div>
            <div className="summary-line">
              <span>Discount</span>
              <strong>-{formatCurrency(discount)}</strong>
            </div>
            <div className="summary-line">
              <span>Shipping</span>
              <strong>{formatCurrency(shippingAmount)}</strong>
            </div>
            <div className="shipping-method-card">
              <div>
                <strong>Standard delivery</strong>
                <span>3-5 business days</span>
              </div>
              <em>{shippingAmount === 0 ? "Free" : formatCurrency(shippingAmount)}</em>
            </div>
            <div className="summary-line">
              <span>Tax</span>
              <strong>{formatCurrency(taxAmount)}</strong>
            </div>
            <div className="summary-line final-total">
              <span>Final total</span>
              <strong>{formatCurrency(grandTotal)}</strong>
            </div>
            <button className="solid-button checkout-button" type="button" disabled={buying} onClick={handleBuyNow}>
              {buying ? "Starting checkout..." : "Proceed to Checkout"}
            </button>
          </section>

          <section className="delivery-card">
            <div className="delivery-title-row">
              <div>
                <h2>Delivery Address</h2>
                <p>Save multiple addresses and choose one for this order.</p>
              </div>
              <button className="ghost-button" type="button" onClick={startNewAddress}>Add new</button>
            </div>
            {addressesLoading ? (
              <p className="saved-address-loading">Loading saved addresses...</p>
            ) : savedAddresses.length > 0 && (
              <div className="saved-address-grid">
                {savedAddresses.map((savedAddress) => (
                  <article className={`saved-address-card${selectedAddressId === savedAddress.id ? " selected" : ""}`} key={savedAddress.id}>
                    <div className="saved-address-header">
                      <strong>{savedAddress.label}</strong>
                      {selectedAddressId === savedAddress.id && <span>Using</span>}
                    </div>
                    <p>{savedAddress.recipientName} {savedAddress.phoneNumber && `| ${savedAddress.phoneNumber}`}</p>
                    <p>
                      {savedAddress.addressLine1}{savedAddress.addressLine2 ? `, ${savedAddress.addressLine2}` : ""}, {savedAddress.city}, {savedAddress.state} {savedAddress.postalCode}, {savedAddress.country}
                    </p>
                    <div className="saved-address-actions">
                      <button type="button" onClick={() => useSavedAddress(savedAddress)}>Use</button>
                      <button type="button" onClick={() => startAddressEdit(savedAddress)}>Edit</button>
                      <button className="delete-address-button" type="button" onClick={() => deleteAddress(savedAddress)}>Delete</button>
                    </div>
                  </article>
                ))}
              </div>
            )}
            <div className="address-editor-header">
              <strong>{editingAddressId ? "Update saved address" : selectedAddressId ? "Selected delivery address" : "New delivery address"}</strong>
              {selectedAddressId && !editingAddressId && <small>Editing a field creates a new address unless you click Edit above.</small>}
            </div>
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
            <div className="address-form-actions">
              <button className="solid-button" type="button" onClick={saveAddress} disabled={savingAddress}>
                {savingAddress ? "Saving..." : editingAddressId ? "Update Address" : "Save Address"}
              </button>
              {editingAddressId && <button className="ghost-button" type="button" onClick={() => useSavedAddress(savedAddresses.find((entry) => entry.id === editingAddressId))}>Cancel Edit</button>}
            </div>
            <div className="map-frame-wrap compact-map">
              <iframe title="Checkout address map" src={`https://www.google.com/maps?q=${mapQuery}&z=15&output=embed`} loading="lazy" referrerPolicy="no-referrer-when-downgrade" />
            </div>
          </section>
        </aside>
      </section>

      <div className="mobile-checkout-bar">
        <strong>{formatCurrency(grandTotal)}</strong>
        <button type="button" disabled={buying} onClick={handleBuyNow}>Checkout</button>
      </div>
    </main>
  );
}
