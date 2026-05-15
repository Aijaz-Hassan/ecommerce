import { useEffect, useState } from "react";
import api from "../api/client";
import AdminSectionMenu from "../components/AdminSectionMenu";

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
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

  return (
    <main className="page">
      <section className="admin-header">
        <div>
          <p className="eyebrow">Admin users</p>
          <h1>Manage customer and admin accounts on a dedicated page.</h1>
          <p className="section-note">Use this screen to review users without product or order clutter.</p>
        </div>
      </section>

      <AdminSectionMenu value="/admin/users" />

      <section className="admin-panel">
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <div className="admin-panel-header">
          <h2>Users</h2>
          <button className="ghost-button" type="button" onClick={loadUsers}>
            Refresh
          </button>
        </div>
        <div className="admin-table">
          {users.map((user) => (
            <article className="admin-row" key={user.id}>
              <div>
                <strong>{user.fullName || user.email}</strong>
                <p>{user.email}</p>
                <p>{user.role}</p>
              </div>
              <button className="danger-button" type="button" onClick={() => handleDeleteUser(user.id)}>
                Delete
              </button>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
