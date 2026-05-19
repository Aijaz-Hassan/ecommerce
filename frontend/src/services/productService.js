import api from "../api/client";
import { fallbackProducts } from "../data/fallbackProducts";

export async function getProducts() {
  try {
    const response = await api.get("/products");
    return {
      products: response.data || [],
      source: "api"
    };
  } catch {
    return {
      products: fallbackProducts,
      source: "preview"
    };
  }
}
