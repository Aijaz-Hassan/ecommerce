import { useEffect, useMemo, useState } from "react";
import { Eye, EyeOff, KeyRound, LockKeyhole } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import LoadingSpinner from "../components/LoadingSpinner";
import { useAuth } from "../context/AuthContext";

export default function ResetPasswordPage() {
  const { validateResetToken, resetPassword } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const [form, setForm] = useState({ newPassword: "", confirmPassword: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [status, setStatus] = useState("checking");
  const [expiresAt, setExpiresAt] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);

  const strongPassword = useMemo(
    () => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(form.newPassword),
    [form.newPassword]
  );

  useEffect(() => {
    let ignore = false;
    async function checkToken() {
      try {
        const result = await validateResetToken(token);
        if (!ignore) {
          setStatus(result.valid ? "valid" : "invalid");
          setExpiresAt(result.expiresAt ? new Date(result.expiresAt) : null);
        }
      } catch {
        if (!ignore) {
          setStatus("invalid");
        }
      }
    }
    checkToken();
    return () => {
      ignore = true;
    };
  }, [token]);

  useEffect(() => {
    if (!expiresAt) {
      return;
    }
    const interval = window.setInterval(() => {
      const nextSeconds = Math.max(0, Math.floor((expiresAt.getTime() - Date.now()) / 1000));
      setSecondsLeft(nextSeconds);
      if (nextSeconds === 0) {
        setStatus("invalid");
      }
    }, 1000);
    return () => window.clearInterval(interval);
  }, [expiresAt]);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    window.setTimeout(() => setToast(null), 3200);
  };

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!strongPassword) {
      showToast("Password must include uppercase, lowercase, number, and special character.", "error");
      return;
    }
    if (form.newPassword !== form.confirmPassword) {
      showToast("Passwords must match.", "error");
      return;
    }
    setLoading(true);
    try {
      await resetPassword({ token, ...form });
      setStatus("success");
      showToast("Password reset successfully. Redirecting to login.");
      window.setTimeout(() => navigate("/login", { replace: true }), 3000);
    } catch (error) {
      showToast(error.response?.data?.message || "Unable to reset password.", "error");
    } finally {
      setLoading(false);
    }
  };

  const minutes = String(Math.floor(secondsLeft / 60)).padStart(2, "0");
  const seconds = String(secondsLeft % 60).padStart(2, "0");

  return (
    <main className="auth-page reset-auth-page">
      {toast && <div className={`floating-toast ${toast.type}`}>{toast.message}</div>}
      <section className="auth-card reset-auth-card">
        <div className="reset-icon-panel">
          <LockKeyhole size={34} />
        </div>
        <div>
          <p className="eyebrow">Reset password</p>
          <h1>Create a secure new password.</h1>
          {status === "valid" && <p className="auth-helper-text">This link expires in {minutes}:{seconds}.</p>}
        </div>

        {status === "checking" && <LoadingSpinner label="Validating reset link..." />}

        {status === "invalid" && (
          <div className="success-panel error-panel">
            <strong>Reset link expired or invalid</strong>
            <p>Please request a new reset link to continue.</p>
            <Link className="solid-link" to="/forgot-password">Request new link</Link>
          </div>
        )}

        {status === "success" && (
          <div className="success-panel">
            <strong>Password updated</strong>
            <p>You will be redirected to login shortly.</p>
          </div>
        )}

        {status === "valid" && (
          <form className="auth-form" onSubmit={handleSubmit}>
            <label className="password-field">
              New password
              <span>
                <input
                  name="newPassword"
                  type={showPassword ? "text" : "password"}
                  placeholder="Strong password"
                  value={form.newPassword}
                  onChange={handleChange}
                  required
                />
                <button type="button" onClick={() => setShowPassword((current) => !current)} aria-label="Toggle password visibility">
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>
            <label className="password-field">
              Confirm password
              <span>
                <input
                  name="confirmPassword"
                  type={showConfirm ? "text" : "password"}
                  placeholder="Confirm password"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  required
                />
                <button type="button" onClick={() => setShowConfirm((current) => !current)} aria-label="Toggle confirm password visibility">
                  {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>
            <div className="password-rules">
              <span className={form.newPassword.length >= 8 ? "valid" : ""}>8+ characters</span>
              <span className={/[A-Z]/.test(form.newPassword) ? "valid" : ""}>Uppercase</span>
              <span className={/[a-z]/.test(form.newPassword) ? "valid" : ""}>Lowercase</span>
              <span className={/\d/.test(form.newPassword) ? "valid" : ""}>Number</span>
              <span className={/[^A-Za-z0-9]/.test(form.newPassword) ? "valid" : ""}>Special</span>
            </div>
            <button className="solid-button" type="submit" disabled={loading}>
              {loading ? <LoadingSpinner label="Resetting..." /> : <><KeyRound size={18} /> Reset Password</>}
            </button>
          </form>
        )}
      </section>
    </main>
  );
}
