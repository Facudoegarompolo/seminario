function FilterTabs({ options, activeKey, onChange }) {
  return (
    <div className="filter-tabs">
      {options.map((option) => (
        <button
          key={option.key}
          type="button"
          className={`filter-tab ${option.key === activeKey ? 'active' : ''}`}
          onClick={() => onChange(option.key)}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

export default FilterTabs
