import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/client";

export default function MyOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadOrders() {
      try {
        const response = await api.get("/purchases/my");
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
    }

    loadOrders();
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">My orders</p>
          <h1>Track every purchase and revisit each order individually.</h1>
        </div>
      </section>

      {status === "loading" ? (
        <div className="loading-panel">Loading your orders...</div>
      ) : orders.length === 0 ? (
        <div className="loading-panel">
          {message || "No orders yet. Once you complete a purchase, it will appear here."}
        </div>
      ) : (
        <section className="orders-grid">
          {orders.map((order) => (
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
              <Link className="solid-link" to={`/orders/${order.id}`}>
                Track order
              </Link>
            </article>
          ))}
        </section>
      )}
    </main>
  );
}
