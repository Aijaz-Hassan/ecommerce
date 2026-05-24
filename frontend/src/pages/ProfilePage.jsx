import { useEffect, useMemo, useState } from "react";
import LoadingSpinner from "../components/LoadingSpinner";
import { useAuth } from "../context/AuthContext";

const emptyProfile = {
  fullName: "",
  email: "",
  phoneNumber: "",
  profilePictureUrl: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
  alternateAddressLine1: "",
  alternateAddressLine2: "",
  alternateCity: "",
  alternateState: "",
  alternatePostalCode: "",
  alternateCountry: ""
};

export default function ProfilePage() {
  const { user, updateProfile } = useAuth();
  const [form, setForm] = useState(emptyProfile);
  const [editing, setEditing] = useState(false);
  const [toast, setToast] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName || "",
        email: user.email || "",
        phoneNumber: user.phoneNumber || "",
        profilePictureUrl: user.profilePictureUrl || "",
        addressLine1: user.addressLine1 || "",
        addressLine2: user.addressLine2 || "",
        city: user.city || "",
        state: user.state || "",
        postalCode: user.postalCode || "",
        country: user.country || "",
        alternateAddressLine1: user.alternateAddressLine1 || "",
        alternateAddressLine2: user.alternateAddressLine2 || "",
        alternateCity: user.alternateCity || "",
        alternateState: user.alternateState || "",
        alternatePostalCode: user.alternatePostalCode || "",
        alternateCountry: user.alternateCountry || ""
      });
    }
  }, [user]);

  const initials = useMemo(
    () =>
      (user?.fullName || user?.email || "LL")
        .split(" ")
        .map((part) => part[0])
        .join("")
        .slice(0, 2)
        .toUpperCase(),
    [user]
  );

  const address = [user?.addressLine1, user?.addressLine2, user?.city, user?.state, user?.postalCode, user?.country]
    .filter(Boolean)
    .join(", ");
  const alternateAddress = [user?.alternateAddressLine1, user?.alternateAddressLine2, user?.alternateCity, user?.alternateState, user?.alternatePostalCode, user?.alternateCountry]
    .filter(Boolean)
    .join(", ");

  const createdAt = user?.createdAt
    ? new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(user.createdAt))
    : "Not available";

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handlePictureUpload = (event) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      setForm((current) => ({ ...current, profilePictureUrl: reader.result }));
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (form.profilePictureUrl && !/^(https?:\/\/.+|data:image\/(png|jpeg|jpg|gif|webp);base64,.+)$/.test(form.profilePictureUrl)) {
      setToast({ type: "error", message: "Profile image must be a valid image URL or uploaded image." });
      return;
    }
    setSaving(true);
    setToast(null);
    try {
      await updateProfile(form);
      setEditing(false);
      setToast({ type: "success", message: "Profile updated successfully." });
    } catch (error) {
      const data = error.response?.data;
      setToast({ type: "error", message: data?.message || Object.values(data || {})[0] || "Unable to update profile." });
    } finally {
      setSaving(false);
    }
  };

  if (!user) {
    return (
      <main className="page">
        <LoadingSpinner label="Loading profile..." />
      </main>
    );
  }

  return (
    <main className="page account-page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Profile</p>
          <h1>Your account details, beautifully organized.</h1>
        </div>
        <button className="solid-button" type="button" onClick={() => setEditing((current) => !current)}>
          {editing ? "Cancel Edit" : "Edit Profile"}
        </button>
      </section>

      {toast && <div className={`toast-message ${toast.type}`}>{toast.message}</div>}

      <section className="profile-detail-layout">
        <aside className="account-card profile-identity-card">
          <div className="profile-avatar profile-avatar-large">
            {user.profilePictureUrl ? <img src={user.profilePictureUrl} alt={user.fullName} /> : <span>{initials}</span>}
          </div>
          <div>
            <h2>{user.fullName}</h2>
            <p>{user.email}</p>
          </div>
          {editing && (
            <label className="ghost-button profile-upload-button">
              Upload image
              <input type="file" accept="image/*" onChange={handlePictureUpload} />
            </label>
          )}
        </aside>

        <section className="account-card profile-details-card">
          {!editing ? (
            <div className="profile-details-grid">
              <article>
                <span>Full name</span>
                <strong>{user.fullName}</strong>
              </article>
              <article>
                <span>Email</span>
                <strong>{user.email}</strong>
              </article>
              <article>
                <span>Phone number</span>
                <strong>{user.phoneNumber || "Not added"}</strong>
              </article>
              <article>
                <span>Account creation date</span>
                <strong>{createdAt}</strong>
              </article>
              <article className="full-span">
                <span>Primary address</span>
                <strong>{address || "No saved address"}</strong>
              </article>
              <article className="full-span">
                <span>Alternate address</span>
                <strong>{alternateAddress || "No saved alternate address"}</strong>
              </article>
            </div>
          ) : (
            <form className="admin-form profile-form" onSubmit={handleSubmit}>
              <label>
                Full name
                <input name="fullName" value={form.fullName} onChange={handleChange} required />
              </label>
              <label>
                Email
                <input name="email" type="email" value={form.email} onChange={handleChange} required />
              </label>
              <label>
                Phone number
                <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="+91 98765 43210" />
              </label>
              <label className="full-span">
                Profile image
                <input name="profilePictureUrl" value={form.profilePictureUrl} onChange={handleChange} placeholder="Paste an image URL or upload one" />
              </label>
              <label>
                Address line 1
                <input name="addressLine1" value={form.addressLine1} onChange={handleChange} />
              </label>
              <label>
                Address line 2
                <input name="addressLine2" value={form.addressLine2} onChange={handleChange} />
              </label>
              <label>
                City
                <input name="city" value={form.city} onChange={handleChange} />
              </label>
              <label>
                State
                <input name="state" value={form.state} onChange={handleChange} />
              </label>
              <label>
                Postal code
                <input name="postalCode" value={form.postalCode} onChange={handleChange} />
              </label>
              <label>
                Country
                <input name="country" value={form.country} onChange={handleChange} />
              </label>
              <h3 className="full-span profile-address-heading">Alternate address</h3>
              <label>
                Alternate address line 1
                <input name="alternateAddressLine1" value={form.alternateAddressLine1} onChange={handleChange} />
              </label>
              <label>
                Alternate address line 2
                <input name="alternateAddressLine2" value={form.alternateAddressLine2} onChange={handleChange} />
              </label>
              <label>
                Alternate city
                <input name="alternateCity" value={form.alternateCity} onChange={handleChange} />
              </label>
              <label>
                Alternate state
                <input name="alternateState" value={form.alternateState} onChange={handleChange} />
              </label>
              <label>
                Alternate postal code
                <input name="alternatePostalCode" value={form.alternatePostalCode} onChange={handleChange} />
              </label>
              <label>
                Alternate country
                <input name="alternateCountry" value={form.alternateCountry} onChange={handleChange} />
              </label>
              <div className="admin-form-actions full-span">
                <button className="solid-button" type="submit" disabled={saving}>
                  {saving ? "Saving..." : "Save Profile"}
                </button>
              </div>
            </form>
          )}
        </section>
      </section>
    </main>
  );
}
