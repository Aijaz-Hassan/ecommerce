import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/client";

export default function MyOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");
  const [busyOrderId, setBusyOrderId] = useState(null);
  const [cancellingOrderId, setCancellingOrderId] = useState(null);
  const [cancelReasons, setCancelReasons] = useState({});

  const loadOrders = async (ignore = false) => {
    try {
      const response = await api.get("/orders/my");
      if (!ignore) {
        setOrders(response.data || []);
        setStatus("ready");
      }
    } catch (error) {
      if (!ignore) {
        setMessage(error.response?.data?.message || "Unable to load your orders.");
        setStatus("ready");
      }
    }
  };

  useEffect(() => {
    let ignore = false;
    loadOrders(ignore);
    return () => {
      ignore = true;
    };
  }, []);

  const handleCancelChange = (orderId, value) => {
    setCancelReasons((current) => ({ ...current, [orderId]: value }));
  };

  const handleCancelOrder = async (orderId) => {
    setBusyOrderId(orderId);
    setMessage("");
    try {
      const reason = (cancelReasons[orderId] || "").trim();
      if (reason.length < 5) {
        setMessage("Please enter a cancellation reason with at least 5 characters.");
        return;
      }

      await api.put(`/orders/my/${orderId}/cancel`, {
        reason
      });
      await loadOrders();
      setCancellingOrderId(null);
      setMessage("Order cancelled and removed successfully.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to cancel order.");
    } finally {
      setBusyOrderId(null);
    }
  };

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">My orders</p>
          <h1>Track active purchases and cancel an order with a proper reason when needed.</h1>
        </div>
      </section>

      {status === "loading" ? (
        <div className="loading-panel">Loading your orders...</div>
      ) : orders.length === 0 ? (
        <div className="loading-panel">
          {message || "No orders yet. Once you complete a purchase, it will appear here."}
        </div>
      ) : (
        <>
          {message && <p className={message.includes("successfully") ? "success-text" : "error-text"}>{message}</p>}
          <section className="orders-grid">
            {orders.map((order) => {
              const canCancel = order.status !== "DELIVERED" && order.status !== "CANCELLED";

              return (
                <article className="admin-panel order-card" key={order.id}>
                  <div className="order-card-header">
                    <div>
                      <p className="eyebrow">Order {order.orderNumber}</p>
                      <h2>{new Date(order.createdAt).toLocaleString()}</h2>
                    </div>
                    <span className="status-badge">{order.status}</span>
                  </div>

                  <div className="order-card-meta">
                    <div>
                      <strong>{order.items.length}</strong>
                      <span>Items</span>
                    </div>
                    <div>
                      <strong>${Number(order.totalAmount).toFixed(2)}</strong>
                      <span>Total</span>
                    </div>
                    <div>
                      <strong>{order.paymentProvider}</strong>
                      <span>Payment</span>
                    </div>
                  </div>

                  <p className="section-note">
                    {order.items.map((item) => `${item.productName} x${item.quantity}`).join(", ")}
                  </p>

                  <div className="order-address-compact">
                    <strong>Delivery address</strong>
                    <p>
                      {order.addressLine1}
                      {order.addressLine2 ? `, ${order.addressLine2}` : ""}
                      {`, ${order.city}, ${order.state} ${order.postalCode}, ${order.country}`}
                    </p>
                  </div>

                  <div className="tracking-summary-box">
                    <strong>{order.trackingLocation}</strong>
                    <p>{order.trackingNote}</p>
                  </div>

                  {order.cancellationReason && (
                    <div className="order-cancel-box">
                      <strong>Cancellation reason</strong>
                      <p>{order.cancellationReason}</p>
                    </div>
                  )}

                  {cancellingOrderId === order.id && canCancel && (
                    <div className="order-cancel-box">
                      <label className="filter-field compact-field">
                        <span>Cancellation reason</span>
                        <textarea
                          rows="3"
                          value={cancelReasons[order.id] || ""}
                          onChange={(event) => handleCancelChange(order.id, event.target.value)}
                          placeholder="Tell us clearly why you want to cancel this order."
                        />
                      </label>
                      <div className="admin-row-actions">
                        <button
                          className="danger-button"
                          type="button"
                          onClick={() => handleCancelOrder(order.id)}
                          disabled={busyOrderId === order.id}
                        >
                          {busyOrderId === order.id ? "Cancelling..." : "Confirm cancellation"}
                        </button>
                        <button
                          className="ghost-button"
                          type="button"
                          onClick={() => setCancellingOrderId(null)}
                          disabled={busyOrderId === order.id}
                        >
                          Close
                        </button>
                      </div>
                    </div>
                  )}

                  <div className="admin-row-actions order-button-row">
                    <Link className="solid-link" to={`/orders/${order.id}`}>
                      Track
                    </Link>
                    {canCancel && (
                      <button
                        className="danger-button"
                        type="button"
                        onClick={() => setCancellingOrderId(cancellingOrderId === order.id ? null : order.id)}
                        disabled={busyOrderId === order.id}
                      >
                        Cancel order
                      </button>
                    )}
                  </div>
                </article>
              );
            })}
          </section>
        </>
      )}
    </main>
  );
}
