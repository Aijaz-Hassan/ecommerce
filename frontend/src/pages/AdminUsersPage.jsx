import { Ban, CheckCircle2, Search, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import api from "../api/client";
import AdminWorkspace from "../components/AdminWorkspace";

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadUsers = async () => {
    try {
      const response = await api.get("/auth/users");
      setUsers(response.data || []);
      setError("");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to load users.");
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleDeleteUser = async (userId) => {
    try {
      await api.delete(`/auth/users/${userId}`);
      setMessage("User deleted successfully.");
      await loadUsers();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to delete user.");
    }
  };

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLowerCase();
    return users.filter((user) => !query || [user.fullName, user.email, user.role].some((value) => String(value || "").toLowerCase().includes(query)));
  }, [search, users]);

  return (
    <AdminWorkspace title="Customers Management" subtitle="Review customer contact details, roles, and account status from the user registry." search={search} onSearchChange={setSearch}>
      <section className="admin-panel">
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <div className="admin-panel-header">
          <div>
            <h2>Customers</h2>
            <p>{filteredUsers.length} matching users</p>
          </div>
          <label className="compact-admin-filter">
            <Search size={16} />
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search customers" />
          </label>
          <button className="ghost-button" type="button" onClick={loadUsers}>
            Refresh
          </button>
        </div>
        <div className="admin-table">
          {filteredUsers.map((user) => (
            <article className="admin-row" key={user.id}>
              <div>
                <strong>{user.fullName || user.email}</strong>
                <p>{user.email}</p>
                <p>{user.phoneNumber || "No phone saved"} | Joined {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "recently"}</p>
              </div>
              <span className="status-badge">{user.role}</span>
              <div className="admin-row-actions">
                <button className="ghost-button" type="button" disabled title="Requires a backend block/unblock endpoint">
                  <Ban size={16} />
                  Block
                </button>
                <button className="ghost-button" type="button" disabled title="Requires a backend block/unblock endpoint">
                  <CheckCircle2 size={16} />
                  Unblock
                </button>
                <button className="danger-button" type="button" onClick={() => handleDeleteUser(user.id)}>
                  <Trash2 size={16} />
                  Delete
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>
    </AdminWorkspace>
  );
}
