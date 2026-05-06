import { createContext, useContext, useEffect, useMemo, useState } from "react";
import api from "../api/client";
import { useAuth } from "./AuthContext";
import { isAdminRole } from "../utils/roles";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated, user } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const isAdmin = isAdminRole(user?.role);

  const syncCart = async () => {
    if (!isAuthenticated || isAdmin) {
      setCartItems([]);
      return [];
    }

    const response = await api.get("/cart");
    const nextItems = (response.data.items || []).map((item) => ({
      id: item.productId,
      cartItemId: item.id,
      name: item.productName,
      category: item.category,
      imageUrl: item.imageUrl,
      price: item.price,
      quantity: item.quantity,
      selectedColor: item.selectedColor || "",
      selectedSize: item.selectedSize || "",
      customizationNote: item.customizationNote || ""
    }));
    setCartItems(nextItems);
    return nextItems;
  };

  useEffect(() => {
    syncCart().catch(() => {
      setCartItems([]);
    });
  }, [isAuthenticated, isAdmin]);

  const ensureCustomerSession = () => {
    if (!isAuthenticated) {
      throw new Error("Please login to use the cart.");
    }
    if (isAdmin) {
      throw new Error("Admin accounts cannot use the cart.");
    }
  };

  const addToCart = async (product, selections = {}) => {
    ensureCustomerSession();
    await api.post("/cart/items", {
      productId: Number(product.id),
      quantity: 1,
      selectedColor: selections.selectedColor || "",
      selectedSize: selections.selectedSize || "",
      customizationNote: selections.customizationNote || ""
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
      quantity: item.quantity + 1,
      selectedColor: item.selectedColor || "",
      selectedSize: item.selectedSize || "",
      customizationNote: item.customizationNote || ""
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
        quantity: item.quantity - 1,
        selectedColor: item.selectedColor || "",
        selectedSize: item.selectedSize || "",
        customizationNote: item.customizationNote || ""
      });
    }
    return syncCart();
  };

  const updateCartItem = async (cartItemId, updates) => {
    ensureCustomerSession();
    const item = cartItems.find((entry) => String(entry.cartItemId) === String(cartItemId));
    if (!item) {
      return;
    }

    await api.put(`/cart/items/${cartItemId}`, {
      productId: Number(item.id),
      quantity: updates.quantity ?? item.quantity,
      selectedColor: updates.selectedColor ?? item.selectedColor ?? "",
      selectedSize: updates.selectedSize ?? item.selectedSize ?? "",
      customizationNote: updates.customizationNote ?? item.customizationNote ?? ""
    });
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
        updateCartItem,
        cartCount,
        cartTotal,
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
