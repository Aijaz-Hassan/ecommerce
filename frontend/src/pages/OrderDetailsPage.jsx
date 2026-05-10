import { Link, useParams } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import api from "../api/client";

const trackingSteps = ["CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"];

export default function OrderDetailsPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadOrder() {
      try {
        const response = await api.get(`/purchases/my/${id}`);
        if (!ignore) {
          setOrder(response.data);
          setStatus("ready");
        }
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load this order.");
          setStatus("ready");
        }
      }
    }

    loadOrder();
    return () => {
      ignore = true;
    };
  }, [id]);

  const activeStepIndex = useMemo(() => {
    if (!order) {
      return 0;
    }
    const index = trackingSteps.indexOf(order.status);
    return index >= 0 ? index : 0;
  }, [order]);

  if (status === "loading") {
    return <main className="page"><div className="loading-panel">Loading order details...</div></main>;
  }

  if (!order) {
    return <main className="page"><div className="loading-panel">{message || "Order not found."}</div></main>;
  }

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Track order</p>
          <h1>{order.orderNumber}</h1>
          <p className="section-note">Placed on {new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <Link className="ghost-link" to="/orders">
          Back to orders
        </Link>
      </section>

      <section className="order-tracker">
        {trackingSteps.map((step, index) => (
          <div className={`tracker-step${index <= activeStepIndex ? " active" : ""}`} key={step}>
            <span className="tracker-dot" />
            <strong>{step.replace("_", " ")}</strong>
          </div>
        ))}
      </section>

      <section className="order-details-layout">
        <div className="admin-panel">
          <h2>Purchased items</h2>
          <div className="admin-table">
            {order.items.map((item) => (
              <article className="cart-item-row order-item-row" key={item.id}>
                <img className="cart-item-image" src={item.imageUrl} alt={item.productName} />
                <div className="cart-item-info">
                  <strong>{item.productName}</strong>
                  <p>{item.category}</p>
                  <p>
                    Color: {item.selectedColor || "Standard"} | Size: {item.selectedSize || "Standard"}
                  </p>
                  {item.customizationNote && <p>Note: {item.customizationNote}</p>}
                </div>
                <div className="order-item-meta">
                  <strong>Qty {item.quantity}</strong>
                  <span>${Number(item.lineTotal).toFixed(2)}</span>
                </div>
              </article>
            ))}
          </div>
        </div>

        <aside className="cart-aside">
          <div className="admin-panel">
            <h2>Bill details</h2>
            <div className="summary-line">
              <span>Subtotal</span>
              <strong>${Number(order.subtotal).toFixed(2)}</strong>
            </div>
            <div className="summary-line">
              <span>Tax</span>
              <strong>${Number(order.taxAmount).toFixed(2)}</strong>
            </div>
            <div className="summary-line">
              <span>Shipping</span>
              <strong>${Number(order.shippingAmount).toFixed(2)}</strong>
            </div>
            <div className="summary-line">
              <span>Total</span>
              <strong>${Number(order.totalAmount).toFixed(2)}</strong>
            </div>
            <div className="summary-line">
              <span>Payment</span>
              <strong>{order.paymentProvider}</strong>
            </div>
            <div className="summary-line">
              <span>Status</span>
              <strong>{order.status}</strong>
            </div>
          </div>
        </aside>
      </section>
    </main>
  );
}
