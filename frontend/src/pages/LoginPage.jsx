import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, resetPassword } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [forgotForm, setForgotForm] = useState({ email: "", newPassword: "" });
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [resetting, setResetting] = useState(false);

  const destination = location.state?.from || "/";

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleForgotChange = (event) => {
    setForgotForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await login(form);
      window.alert("Login successful.");
      navigate(destination, { replace: true });
    } catch (requestError) {
      const data = requestError.response?.data;
      const message =
        data?.message ||
        Object.values(data || {})[0] ||
        (requestError.code === "ERR_NETWORK"
          ? "Backend is not reachable on http://localhost:8082. Start Spring Boot and MySQL first."
          : "Unable to sign in right now.");
      setError(message);
      window.alert(`Login failed: ${message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (event) => {
    event.preventDefault();
    setResetting(true);
    setError("");
    setSuccess("");
    try {
      await resetPassword(forgotForm);
      setForm((current) => ({ ...current, email: forgotForm.email, password: "" }));
      setForgotForm({ email: "", newPassword: "" });
      setShowForgotPassword(false);
      setSuccess("Password updated. You can login with the new password now.");
    } catch (requestError) {
      const data = requestError.response?.data;
      setError(data?.message || Object.values(data || {})[0] || "Unable to update password.");
    } finally {
      setResetting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div>
          <p className="eyebrow">Welcome back</p>
          <h1>Sign in to continue to your storefront.</h1>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input name="email" type="email" placeholder="you@example.com" value={form.email} onChange={handleChange} />
          </label>
          <label>
            Password
            <input name="password" type="password" placeholder="Enter your password" value={form.password} onChange={handleChange} />
          </label>
          <button className="link-button" type="button" onClick={() => setShowForgotPassword((current) => !current)}>
            Forgot password?
          </button>
          {success && <p className="success-text">{success}</p>}
          {error && <p className="error-text">{error}</p>}
          <button className="solid-button" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Login"}
          </button>
        </form>

        {showForgotPassword && (
          <form className="auth-form reset-form" onSubmit={handleForgotPassword}>
            <label>
              Account email
              <input
                name="email"
                type="email"
                placeholder="you@example.com"
                value={forgotForm.email}
                onChange={handleForgotChange}
                required
              />
            </label>
            <label>
              New password
              <input
                name="newPassword"
                type="password"
                placeholder="Choose a new password"
                value={forgotForm.newPassword}
                onChange={handleForgotChange}
                minLength="6"
                required
              />
            </label>
            <button className="ghost-button" type="submit" disabled={resetting}>
              {resetting ? "Updating..." : "Update password"}
            </button>
          </form>
        )}

        <p className="auth-footer">
          No account yet? <Link to="/register">Create one</Link>
        </p>
      </section>
    </main>
  );
}
