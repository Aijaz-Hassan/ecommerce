import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";

const emptyProfile = {
  fullName: "",
  phoneNumber: "",
  profilePictureUrl: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: ""
};

export default function ProfilePage() {
  const { user, updateProfile } = useAuth();
  const [form, setForm] = useState(emptyProfile);
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName || "",
        phoneNumber: user.phoneNumber || "",
        profilePictureUrl: user.profilePictureUrl || "",
        addressLine1: user.addressLine1 || "",
        addressLine2: user.addressLine2 || "",
        city: user.city || "",
        state: user.state || "",
        postalCode: user.postalCode || "",
        country: user.country || ""
      });
    }
  }, [user]);

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
    setSaving(true);
    setMessage("");
    try {
      await updateProfile(form);
      setMessage("Profile updated successfully.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to update profile.");
    } finally {
      setSaving(false);
    }
  };

  const initials = (user?.fullName || user?.email || "LL")
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <main className="page">
      <section className="section-heading">
        <div>
          <p className="eyebrow">Profile</p>
          <h1>Keep your account details and delivery address up to date.</h1>
        </div>
      </section>

      <section className="profile-layout">
        <aside className="admin-panel profile-preview">
          <div className="profile-avatar">
            {form.profilePictureUrl ? <img src={form.profilePictureUrl} alt={form.fullName || "Profile"} /> : <span>{initials}</span>}
          </div>
          <div>
            <h2>{form.fullName || "Your profile"}</h2>
            <p>{user?.email}</p>
          </div>
          <label className="ghost-button profile-upload-button">
            Set profile picture
            <input type="file" accept="image/*" onChange={handlePictureUpload} />
          </label>
        </aside>

        <form className="admin-form profile-form" onSubmit={handleSubmit}>
          <label>
            Full name
            <input name="fullName" value={form.fullName} onChange={handleChange} required />
          </label>
          <label>
            Phone number
            <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="+91 98765 43210" />
          </label>
          <label className="full-span">
            Profile picture URL
            <input
              name="profilePictureUrl"
              value={form.profilePictureUrl}
              onChange={handleChange}
              placeholder="Paste an image URL or use the upload button"
            />
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
          {message && (
            <p className={message.includes("successfully") ? "success-text full-span" : "error-text full-span"}>{message}</p>
          )}
          <div className="admin-form-actions full-span">
            <button className="solid-button" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Update profile"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
