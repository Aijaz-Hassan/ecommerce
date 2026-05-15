import { useNavigate } from "react-router-dom";

const adminSections = [
  { value: "/admin", label: "Overview" },
  { value: "/admin/products", label: "Products" },
  { value: "/admin/users", label: "Users" },
  { value: "/admin/orders", label: "Customer Orders" }
];

export default function AdminSectionMenu({ value }) {
  const navigate = useNavigate();

  return (
    <div className="admin-select-bar">
      <label className="filter-field admin-select-field">
        <span>Admin section</span>
        <select value={value} onChange={(event) => navigate(event.target.value)}>
          {adminSections.map((section) => (
            <option key={section.value} value={section.value}>
              {section.label}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
