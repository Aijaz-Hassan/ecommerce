import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await register(form);
      navigate("/products", { replace: true });
    } catch (requestError) {
      const data = requestError.response?.data;
      const message =
        data?.message ||
        Object.values(data || {})[0] ||
        (requestError.code === "ERR_NETWORK"
          ? "Backend is not reachable on http://localhost:8082. Start Spring Boot and MySQL first."
          : "Unable to create account right now.");
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div>
          <p className="eyebrow">Join the store</p>
          <h1>Create an account and unlock the admin starter seat.</h1>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Full name
            <input name="fullName" type="text" placeholder="Your full name" value={form.fullName} onChange={handleChange} />
          </label>
          <label>
            Email
            <input name="email" type="email" placeholder="you@example.com" value={form.email} onChange={handleChange} />
          </label>
          <label>
            Password
            <input
              name="password"
              type="password"
              placeholder="At least 8 characters with letters and numbers"
              value={form.password}
              onChange={handleChange}
            />
          </label>
          {error && <p className="error-text">{error}</p>}
          <button className="solid-button" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Register"}
          </button>
        </form>

        <p className="auth-footer">
          Already a member? <Link to="/login">Login</Link>
        </p>
      </section>
    </main>
  );
}
