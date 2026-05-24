import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAdminRole } from "../utils/roles";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(location.state?.message || "");
  const [loading, setLoading] = useState(false);

  const destination = location.state?.from || "/";

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const authErrorMessage = (requestError, fallback) => {
    const data = requestError.response?.data;
    return (
      data?.message ||
      Object.values(data || {})[0] ||
      (requestError.code === "ERR_NETWORK"
        ? "The sign-in service is unavailable right now."
        : fallback)
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      const user = await login(form);
      navigate(isAdminRole(user?.role) ? "/admin" : destination, { replace: true });
    } catch (requestError) {
      setError(authErrorMessage(requestError, "Unable to sign in right now."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div>
          <p className="eyebrow">Welcome back</p>
          <h1>Sign in to Lumen Lane.</h1>
          <p className="auth-intro">Access orders, saved addresses and checkout securely.</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
            <label>
              Email
              <input name="email" type="email" autoComplete="email" placeholder="you@example.com" value={form.email} onChange={handleChange} required />
            </label>
            <label>
              Password
              <input name="password" type="password" autoComplete="current-password" placeholder="Enter your password" value={form.password} onChange={handleChange} required />
            </label>
            <Link className="link-button forgot-link" to="/forgot-password">
              Forgot password?
            </Link>
            {success && <p className="success-text">{success}</p>}
            {error && <p className="error-text">{error}</p>}
            <button className="solid-button" type="submit" disabled={loading}>
              {loading ? "Signing in..." : "Sign in"}
            </button>
        </form>

        <p className="auth-footer">
          No account yet? <Link to="/register">Create one</Link>
        </p>
      </section>
    </main>
  );
}
