import { useNavigate } from 'react-router-dom'
import '../estilos/NavigationButtons.css'

function StatsButton() {
  const navigate = useNavigate()

  return (
    <button
      className="nav-stats-button"
      type="button"
      onClick={() => navigate('/admin/estadisticas')}
      aria-label="Ver estadísticas"
      title="Estadísticas"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="12" y1="2" x2="12" y2="22" />
        <path d="M17 5h-10a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z" />
        <line x1="6" y1="12" x2="18" y2="12" />
      </svg>
    </button>
  )
}

export default StatsButton
