import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import dashboardService from '../../shared/services/dashboardService'
import RealtimeActivityList from '../componentes/RealtimeActivityList'
import logo from '../../shared/assets/logo.jpeg'
import '../estilos/Dashboard.css'

const placeholderSummary = {
  waiting: 23,
  averageWait: '15 min',
  servedToday: 128,
  noShows: 12,
  recent: [
    { id: '1', number: 23, status: 'Llamado', time: '9:28 PM' },
    { id: '2', number: 22, status: 'Atendido', time: '9:27 PM' },
    { id: '3', number: 21, status: 'Atendido', time: '9:25 PM' },
    { id: '4', number: 20, status: 'No se presentó', time: '9:20 PM' },
  ],
}

function Dashboard() {
  const [summary, setSummary] = useState(placeholderSummary)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const data = await dashboardService.getSummary()
        setSummary({
          waiting: data.waiting ?? placeholderSummary.waiting,
          averageWait: data.averageWait ?? placeholderSummary.averageWait,
          servedToday: data.servedToday ?? placeholderSummary.servedToday,
          noShows: data.noShows ?? placeholderSummary.noShows,
          recent: data.recent ?? placeholderSummary.recent,
        })
      } catch (error) {
        console.warn('No se pudo cargar el resumen, se usan datos de ejemplo', error)
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
