import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const initialForm = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: ""
};

export default function UpdatePasswordPage() {
  const navigate = useNavigate();
  const { updatePassword } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [message, setMessage] = useState(null);
  const [saving, setSaving] = useState(false);

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const validate = () => {
    if (form.newPassword.length < 8) {
      return "Password must be at least 8 characters.";
    }
    if (form.newPassword !== form.confirmPassword) {
      return "New password and confirm password must match.";
    }
    if (form.currentPassword === form.newPassword) {
      return "New password must be different from the current password.";
    }
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const validationError = validate();
    if (validationError) {
      setMessage({ type: "error", text: validationError });
      return;
    }

    setSaving(true);
    setMessage(null);
    try {
      await updatePassword(form);
      navigate("/login", {
        replace: true,
        state: { message: "Password updated successfully. Please login with your new password." }
      });
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.message || "Unable to update password." });
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="page account-page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Security</p>
          <h1>Update your password.</h1>
        </div>
      </section>

      <section className="account-card password-card">
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Current password
            <input name="currentPassword" type="password" value={form.currentPassword} onChange={handleChange} required autoComplete="current-password" />
          </label>
          <label>
            New password
            <input name="newPassword" type="password" value={form.newPassword} onChange={handleChange} required minLength={8} autoComplete="new-password" />
          </label>
          <label>
            Confirm new password
            <input name="confirmPassword" type="password" value={form.confirmPassword} onChange={handleChange} required autoComplete="new-password" />
          </label>
          {message && <p className={message.type === "success" ? "success-text" : "error-text"}>{message.text}</p>}
          <button className="solid-button" type="submit" disabled={saving}>
            {saving ? "Updating..." : "Update Password"}
          </button>
        </form>
      </section>
    </main>
  );
}
