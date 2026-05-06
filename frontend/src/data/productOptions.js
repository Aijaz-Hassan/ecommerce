export function getProductOptions(product) {
  const category = product?.category || "";

  const colorMap = {
    Audio: ["Black", "White", "Blue", "Red"],
    Workspace: ["Black", "Silver", "Graphite", "Ivory"],
    Wearables: ["Black", "Rose Gold", "Silver", "Navy"],
    Travel: ["Black", "Olive", "Sand", "Navy"],
    Lighting: ["Black", "White", "Gold", "Silver"],
    Entertainment: ["Black", "Silver", "Midnight Blue"]
  };

  const sizeMap = {
    Audio: ["Standard", "Compact", "Pro"],
    Workspace: ["Mini", "Standard", "Full Size"],
    Wearables: ["Small", "Medium", "Large"],
    Travel: ["Small", "Medium", "Large"],
    Lighting: ["Desk", "Standard", "Large"],
    Entertainment: ["Portable", "Standard", "Premium"]
  };

  return {
    colors: colorMap[category] || ["Black", "Silver", "Blue"],
    sizes: sizeMap[category] || ["Standard", "Medium", "Large"]
  };
}
