import { createContext, useContext, useEffect, useMemo, useState } from "react";
import api from "../api/client";
import { useAuth } from "./AuthContext";
import { isAdminRole } from "../utils/roles";

const CartContext = createContext(null);
const cartStoragePrefix = "lumenlane_cart_snapshot";

const getCartStorageKey = (user) => {
  const identity = user?.id || user?.email || "anonymous";
  return `${cartStoragePrefix}_${identity}`;
};

const readSavedCart = (user) => {
  if (!user) {
    return [];
  }
  try {
    const saved = window.localStorage.getItem(getCartStorageKey(user));
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
};

export function CartProvider({ children }) {
  const { isAuthenticated, user } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const [cartLoading, setCartLoading] = useState(false);
  const isAdmin = isAdminRole(user?.role);

  const syncCart = async () => {
    if (!isAuthenticated || isAdmin) {
      setCartItems([]);
      setCartLoading(false);
      return [];
    }

    setCartLoading(true);
    try {
      const response = await api.get("/cart");
      const nextItems = (response.data.items || []).map((item) => ({
        id: item.productId,
        cartItemId: item.id,
        name: item.productName,
        brand: item.brand || item.category || "Lumen Lane",
        category: item.category,
        imageUrl: item.imageUrl,
        price: item.price,
        quantity: item.quantity,
        selectedColor: item.selectedColor || "",
        selectedSize: item.selectedSize || "",
        lineTotal: item.lineTotal
      }));
      setCartItems(nextItems);
      return nextItems;
    } finally {
      setCartLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated && !isAdmin && user) {
      setCartItems(readSavedCart(user));
      return;
    }
    setCartItems([]);
  }, [isAuthenticated, isAdmin, user?.id, user?.email]);

  useEffect(() => {
    if (isAuthenticated && !isAdmin && user) {
      window.localStorage.setItem(getCartStorageKey(user), JSON.stringify(cartItems));
    }
  }, [cartItems, isAuthenticated, isAdmin, user]);

  useEffect(() => {
    syncCart().catch(() => {
      setCartItems([]);
      setCartLoading(false);
    });
  }, [isAuthenticated, isAdmin, user?.id, user?.email]);

  const ensureCustomerSession = () => {
    if (!isAuthenticated) {
      throw new Error("Please login to use the cart.");
    }
    if (isAdmin) {
      throw new Error("Admin accounts cannot use the cart.");
    }
  };

  const addToCart = async (product, quantity = 1) => {
    ensureCustomerSession();
    await api.post("/cart/items", {
      productId: Number(product.id),
      quantity
    });
    return syncCart();
  };

  const removeFromCart = async (cartItemId) => {
    ensureCustomerSession();
    await api.delete(`/cart/items/${cartItemId}`);
    return syncCart();
  };

  const increaseQuantity = async (cartItemId) => {
    ensureCustomerSession();
    const item = cartItems.find((entry) => String(entry.cartItemId) === String(cartItemId));
    if (!item) {
      return;
    }

    await api.put(`/cart/items/${cartItemId}`, {
      productId: Number(item.id),
      quantity: item.quantity + 1
    });
    return syncCart();
  };

  const decreaseQuantity = async (cartItemId) => {
    ensureCustomerSession();
    const item = cartItems.find((entry) => String(entry.cartItemId) === String(cartItemId));
    if (!item) {
      return;
    }

    if (item.quantity <= 1) {
      await api.delete(`/cart/items/${cartItemId}`);
    } else {
      await api.put(`/cart/items/${cartItemId}`, {
        productId: Number(item.id),
        quantity: item.quantity - 1
      });
    }
    return syncCart();
  };

  const clearCart = async () => {
    ensureCustomerSession();
    await api.delete("/cart");
    setCartItems([]);
  };

  const cartCount = useMemo(() => cartItems.reduce((total, item) => total + item.quantity, 0), [cartItems]);
  const cartTotal = useMemo(() => cartItems.reduce((total, item) => total + Number(item.price) * item.quantity, 0), [cartItems]);

  return (
    <CartContext.Provider
      value={{
        cartItems,
        addToCart,
        removeFromCart,
        clearCart,
        increaseQuantity,
        decreaseQuantity,
        cartCount,
        cartTotal,
        cartLoading,
        refreshCart: syncCart
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used inside CartProvider");
  }
  return context;
}
