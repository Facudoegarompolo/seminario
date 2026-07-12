import { useCallback, useEffect, useMemo, useState } from 'react'
import queueService from '../../shared/services/queueService'
import FilterTabs from '../componentes/FilterTabs'
import BackButton from '../componentes/BackButton'
import StatsButton from '../componentes/StatsButton'
import { useNavigate } from 'react-router-dom'
import '../estilos/Fila.css'

const TAB_OPTIONS = [
  { key: 'WAITING', label: 'En espera' },
  { key: 'ALL', label: 'Todos' },
  { key: 'CALLED', label: 'Llamados' },
  { key: 'FINISHED', label: 'Atendidos' },
]

const STATUS_MAP = {
  ESPERANDO: { label: 'En espera', color: 'green' },
  PROXIMO: { label: 'En espera', color: 'green' },
  LLAMADO: { label: 'Llamado', color: 'blue' },
  ATENDIENDO: { label: 'Atendido', color: 'gray' },
  FINALIZADO: { label: 'Atendido', color: 'gray' },
  CANCELADO: { label: 'Cancelado', color: 'red' },
  NO_PRESENTADO: { label: 'No se presentó', color: 'red' },
  EXPIRADO: { label: 'Expirado', color: 'red' },
}

const REFRESH_INTERVAL_MS = 5000
const TERMINAL_STATES = ['FINALIZADO', 'NO_PRESENTADO', 'CANCELADO', 'EXPIRADO']
const ordenarTurnosPorPrioridad = (turnos) => {
  return [...turnos].sort((a, b) => {
    if (a.prioridad && !b.prioridad) return -1
    if (!a.prioridad && b.prioridad) return 1

    if (a.prioridad && b.prioridad) {
      return new Date(a.fechaSolicitudPrioridad) - new Date(b.fechaSolicitudPrioridad)
    }

    return new Date(a.createdAt) - new Date(b.createdAt)
  })
}

function Fila() {
  const navigate = useNavigate()
  const [turnos, setTurnos] = useState([])
  const [activeTab, setActiveTab] = useState('WAITING')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [successMessage, setSuccessMessage] = useState(null)
  const [actionLoading, setActionLoading] = useState(false)
  const [turnoRemovingId, setTurnoRemovingId] = useState(null)

  const fetchTurnos = useCallback(async ({ showLoading = false } = {}) => {
    if (showLoading) {
      setLoading(true)
    }

    try {
      const data = await queueService.getTurnos()
      setTurnos(Array.isArray(data) ? data : [])
      setError(null)
    } catch {
      setError('No se pudo cargar la fila. Intenta nuevamente.')
    } finally {
      if (showLoading) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      fetchTurnos({ showLoading: true })
    }, 0)

    const interval = window.setInterval(() => {
      fetchTurnos()
    }, REFRESH_INTERVAL_MS)

    return () => {
      window.clearTimeout(timer)
      window.clearInterval(interval)
    }
  }, [fetchTurnos])

  const enrichedTurnos = useMemo(() => {
    return turnos.map((turno) => {
      const statusInfo = STATUS_MAP[turno.estado] || { label: turno.estado, color: 'gray' }
      const personas = turno.cantidadIntegrantes ?? 1
      const tiempoEstimado = `${turno.tiempoEstimadoInformadoMinutos ?? 0} min`

      return {
        ...turno,
        estadoLabel: statusInfo.label,
        statusColor: statusInfo.color,
        personas,
        tiempoEstimado,
      }
    })
  }, [turnos])

  const filteredTurnos = useMemo(() => {
    let turnosFiltrados = enrichedTurnos

    if (activeTab === 'WAITING') {
      turnosFiltrados = enrichedTurnos.filter((turno) =>
        ['ESPERANDO', 'PROXIMO'].includes(turno.estado)
      )
    }

    if (activeTab === 'CALLED') {
      turnosFiltrados = enrichedTurnos.filter((turno) =>
        ['LLAMADO', 'ATENDIENDO'].includes(turno.estado)
      )
    }

    if (activeTab === 'FINISHED') {
      turnosFiltrados = enrichedTurnos.filter((turno) =>
        TERMINAL_STATES.includes(turno.estado)
      )
    }

    return ordenarTurnosPorPrioridad(turnosFiltrados)
  }, [activeTab, enrichedTurnos])

  const handleCallNext = () => {
    navigate('/admin/llamar')
  }

  const handleRemoveFinished = async () => {
    const confirmed = window.confirm('¿Estas seguro que quieres quitar atendidos? Si dices que si, se eliminaran pero quedaran en las estadisticas')
    if (!confirmed) return

    setActionLoading(true)
    setError(null)
    setSuccessMessage(null)

    try {
      const response = await queueService.quitarAtendidos()
      await fetchTurnos()
      const count = response?.turnosOcultados ?? 0
      setSuccessMessage(
        count === 0
          ? 'No habia turnos atendidos para quitar.'
          : `Se quitaron ${count} turnos atendidos de la fila virtual.`
      )
    } catch {
      setError('No se pudieron quitar los turnos atendidos.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleRemoveTurno = async (turno) => {
    const confirmed = window.confirm(`Quitar el turno #${turno.numeroTurno} de la fila virtual? Las estadisticas se conservan.`)
    if (!confirmed) return

    setTurnoRemovingId(turno.turnoId)
    setError(null)
    setSuccessMessage(null)

    try {
      await queueService.quitarDeFilaVirtual(turno.turnoId)
      await fetchTurnos()
      setSuccessMessage(`Turno #${turno.numeroTurno} quitado de la fila virtual.`)
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo quitar el turno de la fila virtual.')
    } finally {
      setTurnoRemovingId(null)
    }
  }

  return (
    <div className="fila-page">
      <BackButton to="/admin/dashboard" />
      <StatsButton />
      <header className="fila-header">
        <div>
          <h2>Fila virtual</h2>
          <p>Lista completa de la fila con acciones disponibles.</p>
        </div>
        <button
          className="fila-secondary-button"
          type="button"
          onClick={handleRemoveFinished}
          disabled={actionLoading}
        >
          {actionLoading ? 'Quitando...' : 'Quitar atendidos'}
        </button>
      </header>

      <FilterTabs options={TAB_OPTIONS} activeKey={activeTab} onChange={setActiveTab} />

      {error && <div className="fila-error">{error}</div>}
      {successMessage && <div className="fila-success">{successMessage}</div>}

      <div className="fila-list">
        {loading ? (
          <div className="fila-loading">Cargando turnos...</div>
        ) : filteredTurnos.length === 0 ? (
          <div className="fila-empty">No hay turnos en esta categoría</div>
        ) : (
          filteredTurnos.map((turno) => (
            <div key={turno.turnoId} className="fila-item">
              <div className="fila-item-left">
                <div className="fila-item-title">
                  <h3>{turno.nombreCliente || 'Cliente anónimo'} · #{turno.numeroTurno}</h3>

                  {turno.prioridad && (
                    <span className="fila-prioridad-badge">
                      PRIORIDAD
                    </span>
                  )}
                </div>

                <div className="fila-item-meta">
                  <span className={`status-dot ${turno.statusColor}`} />
                  <span className="fila-status">
                    {turno.estadoLabel}
                  </span>

                  <span className="fila-personas">
                    • {turno.personas} personas
                  </span>
                </div>
              </div>

              <div className="fila-item-right">
                {['ESPERANDO', 'PROXIMO'].includes(turno.estado) ? (
                  <>
                    <strong>{turno.tiempoEstimado}</strong>
                    <span>aprox.</span>
                  </>
                ) : (
                  <strong>—</strong>
                )}

                {TERMINAL_STATES.includes(turno.estado) && (
                  <button
                    className="fila-remove-button"
                    type="button"
                    onClick={() => handleRemoveTurno(turno)}
                    disabled={turnoRemovingId === turno.turnoId}
                  >
                    {turnoRemovingId === turno.turnoId ? 'Quitando...' : 'Quitar de fila'}
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <button className="fila-primary-button" type="button" onClick={handleCallNext}>
        Llamar siguiente
      </button>
    </div>
  )
}

export default Fila
