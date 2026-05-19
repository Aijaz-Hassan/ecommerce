import { useEffect, useMemo, useState } from "react";
import { getProducts } from "../services/productService";

export function useProducts() {
  const [products, setProducts] = useState([]);
  const [source, setSource] = useState("api");
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let ignore = false;

    async function loadProducts() {
      setStatus("loading");
      const result = await getProducts();
      if (!ignore) {
        setProducts(result.products);
        setSource(result.source);
        setStatus("ready");
      }
    }

    loadProducts();
    return () => {
      ignore = true;
    };
  }, []);

  const categories = useMemo(() => [...new Set(products.map((product) => product.category).filter(Boolean))], [products]);

  return {
    products,
    categories,
    source,
    status
  };
}
