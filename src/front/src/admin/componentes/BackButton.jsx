import { useNavigate } from 'react-router-dom'
import '../estilos/NavigationButtons.css'

function BackButton({ to = '/admin/dashboard' }) {
  const navigate = useNavigate()

  return (
    <button
      className="nav-back-button"
      type="button"
      onClick={() => navigate(to)}
      aria-label="Volver atrás"
      title="Volver"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M15 18l-6-6 6-6" />
      </svg>
    </button>
  )
}

export default BackButton
