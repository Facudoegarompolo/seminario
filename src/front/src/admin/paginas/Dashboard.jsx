import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import dashboardService from '../../shared/services/dashboardService'
import RealtimeActivityList from '../componentes/RealtimeActivityList'
import StatsButton from '../componentes/StatsButton'
import logo from '../../shared/assets/logo.jpeg'
import '../estilos/Dashboard.css'

function Dashboard() {
  const [summary, setSummary] = useState({
    waiting: 0,
    averageWait: '0 min',
    servedToday: 0,
    noShows: 0,
    recent: [],
  })

  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const data = await dashboardService.getSummary()

        setSummary({
          waiting: data.waiting ?? 0,
          averageWait: data.averageWait ?? '0 min',
          servedToday: data.servedToday ?? 0,
          noShows: data.noShows ?? 0,
          recent: Array.isArray(data.recent) ? data.recent : [],
        })
      } catch (error) {
        console.error('No se pudo cargar el dashboard', error)
      } finally {
        setLoading(false)
      }
    }

    fetchSummary()
  }, [])

  const currentDate = new Date().toLocaleDateString('es-AR', {
    day: '2-digit',
    month: 'short',
  })

  return (
    <div className="dashboard-page">
      <StatsButton />

      <div className="dashboard-logo-small">
        <img src={logo} alt="DQ" />
      </div>

      <section className="dashboard-header-card">
        <div>
          <span className="page-label">¡Hola, Admin!</span>
          <h2>Resumen del día</h2>
          <p>Hoy, {currentDate}</p>
        </div>

        <div className="dashboard-summary">
          <div className="summary-item">
            <div className="summary-item-label">
              <span>Fila activa</span>
              <span className="summary-icon icon-users" />
            </div>

            <strong>{loading ? '...' : summary.waiting}</strong>

            <p>personas</p>
          </div>

          <div className="summary-item">
            <div className="summary-item-label">
              <span>Tiempo de espera estimado</span>
              <span className="summary-icon icon-clock" />
            </div>

            <strong>{loading ? '...' : summary.averageWait}</strong>

            <p>min</p>
          </div>

          <div className="summary-item">
            <div className="summary-item-label">
              <span>Atendidos hoy</span>
              <span className="summary-icon icon-check" />
            </div>

            <strong>{loading ? '...' : summary.servedToday}</strong>
          </div>

          <div className="summary-item summary-danger">
            <div className="summary-item-label">
              <span>No se presentaron</span>
              <span className="summary-icon icon-user-red" />
            </div>

            <strong>{loading ? '...' : summary.noShows}</strong>
          </div>
        </div>
      </section>

      <section className="dashboard-activity-card">
        <div className="activity-header">
          <h3>Actividad en tiempo real</h3>
        </div>

        <RealtimeActivityList events={summary.recent} />

        <Link to="/admin/fila" className="view-queue-button">
          Ver fila completa
        </Link>
      </section>
    </div>
  )
}

export default Dashboard