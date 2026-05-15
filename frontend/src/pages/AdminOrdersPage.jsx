import { useEffect, useMemo, useState } from "react";
import api from "../api/client";
import AdminSectionMenu from "../components/AdminSectionMenu";

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [products, setProducts] = useState([]);
  const [error, setError] = useState("");

  const loadData = async () => {
    try {
      const [ordersResponse, productsResponse] = await Promise.all([
        api.get("/orders/admin-summary"),
        api.get("/products")
      ]);
      setOrders(ordersResponse.data || []);
      setProducts(productsResponse.data || []);
      setError("");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to load customer orders.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const totalUnitsLeft = useMemo(
    () => products.reduce((sum, product) => sum + Number(product.stock || 0), 0),
    [products]
  );

  return (
    <main className="page">
      <section className="admin-header">
        <div>
          <p className="eyebrow">Admin customer orders</p>
          <h1>See who purchased what and track how much stock is still left.</h1>
          <p className="section-note">This page combines customer purchases with the remaining stock for each ordered item.</p>
        </div>
      </section>

      <AdminSectionMenu value="/admin/orders" />

      <section className="stats-band">
        <article>
          <span>Total customer orders</span>
          <strong>{orders.length}</strong>
        </article>
        <article>
          <span>Products in catalog</span>
          <strong>{products.length}</strong>
        </article>
        <article>
          <span>Total units left</span>
          <strong>{totalUnitsLeft}</strong>
        </article>
      </section>

      <section className="admin-panel">
        {error && <p className="error-text">{error}</p>}
        <div className="admin-panel-header">
          <h2>Purchased orders</h2>
          <button className="ghost-button" type="button" onClick={loadData}>
            Refresh
          </button>
        </div>
        <div className="admin-table">
          {orders.length === 0 ? (
            <article className="admin-row">
              <div>
                <strong>No purchases yet</strong>
                <p>Customer purchases will appear here once checkout happens.</p>
              </div>
            </article>
          ) : (
            orders.map((order) => (
              <article className="admin-row admin-order-row" key={order.orderId}>
                <div className="admin-order-block">
                  <strong>{order.customerName}</strong>
                  <p>{order.customerEmail}</p>
                  <p>Order {order.orderNumber}</p>
                  <p>Status: {order.status}</p>
                </div>
                <div className="admin-order-block">
                  <strong>Purchased items</strong>
                  {order.items.map((item) => (
                    <p key={`${order.orderId}-${item.productId}`}>
                      {item.productName} x{item.orderedQuantity} | Stock left: {item.remainingStock}
                    </p>
                  ))}
                </div>
                <div className="admin-order-block">
                  <strong>Total</strong>
                  <p>${Number(order.totalAmount).toFixed(2)}</p>
                  <p>{new Date(order.createdAt).toLocaleString()}</p>
                </div>
              </article>
            ))
          )}
        </div>
      </section>

      <section className="admin-panel">
        <div className="admin-panel-header">
          <h2>Current stock by product</h2>
        </div>
        <div className="admin-table">
          {products.map((product) => (
            <article className="admin-row" key={product.id}>
              <div>
                <strong>{product.name}</strong>
                <p>{product.category}</p>
              </div>
              <div className="stock-pill">
                <strong>{product.stock}</strong>
                <span>Units left</span>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
