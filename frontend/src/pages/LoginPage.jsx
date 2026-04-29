import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const destination = location.state?.from || "/products";

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
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

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div>
          <p className="eyebrow">Welcome back</p>
          <h1>Sign in to manage your picks and publish products.</h1>
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
          {error && <p className="error-text">{error}</p>}
          <button className="solid-button" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Login"}
          </button>
        </form>

        <p className="auth-footer">
          No account yet? <Link to="/register">Create one</Link>
        </p>
      </section>
    </main>
  );
}
