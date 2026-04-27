import { Link } from "react-router-dom";
import { heroStats, highlights } from "../data/highlights";

export default function HomePage() {
  return (
    <main className="page">
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Next-wave ecommerce</p>
          <h1>Design-led shopping for standout digital lifestyles.</h1>
          <p className="hero-text">
            Lumen Lane blends a fashion-editorial look with real commerce plumbing, giving your project a storefront
            that feels polished instead of generic.
          </p>
          <div className="hero-actions">
            <Link className="solid-link" to="/products">
              Explore products
            </Link>
            <Link className="ghost-link" to="/register">
              Create account
            </Link>
          </div>
        </div>

        <div className="hero-visual">
          <div className="visual-card visual-primary">
            <span className="tag">Featured</span>
            <h3>Aurora Noise-Cancel Headset</h3>
            <p>Immersive audio wrapped in a studio-grade silhouette.</p>
          </div>
          <div className="visual-card visual-secondary">
            <span className="tag">New arrival</span>
            <h3>Pulse Smart Lamp</h3>
            <p>Adaptive light scenes tuned for work, rest, and play.</p>
          </div>
        </div>
      </section>

      <section className="stats-band">
        {heroStats.map((stat) => (
          <article key={stat.label}>
            <strong>{stat.value}</strong>
            <span>{stat.label}</span>
          </article>
        ))}
      </section>

      <section className="highlights-grid">
        {highlights.map((item) => (
          <article key={item.title} className="feature-panel">
            <h3>{item.title}</h3>
            <p>{item.text}</p>
          </article>
        ))}
      </section>
    </main>
  );
}
