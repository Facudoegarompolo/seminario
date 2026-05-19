function StatsCard({ title, value, description, variant, icon }) {
  return (
    <article className={`stat-card ${variant || ''}`}>
      <div className="stat-card-top">
        <span>{title}</span>
        <span className={`stat-card-icon ${icon || ''}`} />
      </div>
      <strong>{value}</strong>
      <p>{description}</p>
    </article>
  )
}

export default StatsCard
