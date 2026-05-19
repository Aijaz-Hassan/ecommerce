import { useEffect, useState } from "react";
import ConfirmationModal from "../components/ConfirmationModal";
import { useAuth } from "../context/AuthContext";

const defaultSettings = {
  darkModeEnabled: false,
  orderNotificationsEnabled: true,
  marketingNotificationsEnabled: false,
  language: "en"
};

export default function SettingsPage() {
  const { user, updateSettings, deleteAccount } = useAuth();
  const [settings, setSettings] = useState(defaultSettings);
  const [message, setMessage] = useState(null);
  const [saving, setSaving] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  useEffect(() => {
    if (user) {
      setSettings({
        darkModeEnabled: Boolean(user.darkModeEnabled),
        orderNotificationsEnabled: user.orderNotificationsEnabled ?? true,
        marketingNotificationsEnabled: Boolean(user.marketingNotificationsEnabled),
        language: user.language || "en"
      });
    }
  }, [user]);

  const handleToggle = (name) => {
    setSettings((current) => ({ ...current, [name]: !current[name] }));
  };

  const handleLanguageChange = (event) => {
    setSettings((current) => ({ ...current, language: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      await updateSettings(settings);
      setMessage({ type: "success", text: "Settings updated successfully." });
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.message || "Unable to update settings." });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteAccount();
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.message || "Unable to delete account." });
      setDeleteModalOpen(false);
    }
  };

  return (
    <main className="page account-page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Settings</p>
          <h1>Personalize your account.</h1>
        </div>
      </section>

      {message && <div className={`toast-message ${message.type}`}>{message.text}</div>}

      <form className="settings-layout" onSubmit={handleSubmit}>
        <section className="account-card settings-panel">
          <div className="settings-row">
            <div>
              <strong>Dark mode</strong>
              <p>Switch to a deeper contrast theme.</p>
            </div>
            <button className={`toggle-switch${settings.darkModeEnabled ? " active" : ""}`} type="button" onClick={() => handleToggle("darkModeEnabled")} aria-pressed={settings.darkModeEnabled}>
              <span />
            </button>
          </div>
          <div className="settings-row">
            <div>
              <strong>Order notifications</strong>
              <p>Receive updates about confirmations, shipping, and delivery.</p>
            </div>
            <button className={`toggle-switch${settings.orderNotificationsEnabled ? " active" : ""}`} type="button" onClick={() => handleToggle("orderNotificationsEnabled")} aria-pressed={settings.orderNotificationsEnabled}>
              <span />
            </button>
          </div>
          <div className="settings-row">
            <div>
              <strong>Promotional notifications</strong>
              <p>Get occasional launch notes and curated offers.</p>
            </div>
            <button className={`toggle-switch${settings.marketingNotificationsEnabled ? " active" : ""}`} type="button" onClick={() => handleToggle("marketingNotificationsEnabled")} aria-pressed={settings.marketingNotificationsEnabled}>
              <span />
            </button>
          </div>
          <label className="settings-select">
            Language
            <select value={settings.language} onChange={handleLanguageChange}>
              <option value="en">English</option>
              <option value="hi">Hindi</option>
              <option value="es">Spanish</option>
              <option value="fr">French</option>
              <option value="de">German</option>
            </select>
          </label>
          <button className="solid-button settings-save" type="submit" disabled={saving}>
            {saving ? "Saving..." : "Save Settings"}
          </button>
        </section>

        <aside className="account-card danger-zone">
          <h2>Delete account</h2>
          <p>This removes your login access and saved profile data. Active order records may still be retained for store operations.</p>
          <button className="danger-button" type="button" onClick={() => setDeleteModalOpen(true)}>
            Delete Account
          </button>
        </aside>
      </form>

      <ConfirmationModal
        open={deleteModalOpen}
        title="Delete account?"
        message="This action cannot be undone."
        confirmLabel="Delete Account"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleteModalOpen(false)}
      />
    </main>
  );
}
