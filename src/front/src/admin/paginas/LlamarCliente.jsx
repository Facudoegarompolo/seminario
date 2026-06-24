import { useCallback, useEffect, useState } from 'react'
import queueService from '../../shared/services/queueService'
import BackButton from '../componentes/BackButton'
import StatsButton from '../componentes/StatsButton'
import '../estilos/LlamarCliente.css'

const REFRESH_INTERVAL_MS = 5000
const ESTADOS_EN_ATENCION = ['LLAMADO', 'ATENDIENDO']
const ESTADOS_EN_ESPERA = ['ESPERANDO', 'PROXIMO']

function seleccionarTurno(turnos, ignoredTurnoId) {
  const turnosDisponibles = turnos.filter((item) => item.turnoId !== ignoredTurnoId)
  const enAtencion = turnosDisponibles.find((item) => ESTADOS_EN_ATENCION.includes(item.estado))
  const siguiente = turnosDisponibles.find((item) => ESTADOS_EN_ESPERA.includes(item.estado))

  return {
    turnoActual: enAtencion || siguiente || null,
    turnoLlamado: Boolean(enAtencion),
  }
}

function LlamarCliente() {
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [turnoLlamado, setTurnoLlamado] = useState(false)
  const [message, setMessage] = useState('Cargando el siguiente cliente...')

  const fetchTurno = useCallback(async ({ showLoading = false, ignoredTurnoId } = {}) => {
    if (showLoading) {
      setLoading(true)
    }

    try {
      const turnos = await queueService.getTurnos()
      const { turnoActual, turnoLlamado: hayTurnoLlamado } = seleccionarTurno(turnos, ignoredTurnoId)

      if (turnoActual) {
        setTurno(turnoActual)
        setTurnoLlamado(hayTurnoLlamado)
        setMessage(hayTurnoLlamado
          ? 'Esperando que el turno llamado sea atendido'
          : 'Listo para llamar al siguiente cliente')
      } else {
        setTurno(null)
        setTurnoLlamado(false)
        setMessage('No hay clientes en espera en este momento')
      }
    } catch {
      setMessage('No se pudo cargar el siguiente cliente.')
    } finally {
      if (showLoading) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      fetchTurno({ showLoading: true })
    }, 0)
    const interval = window.setInterval(() => {
      fetchTurno()
    }, REFRESH_INTERVAL_MS)

    return () => {
      window.clearTimeout(timer)
      window.clearInterval(interval)
    }
  }, [fetchTurno])

  const handleCall = async () => {
    if (!turno) return

    setBusy(true)

    try {
      await queueService.llamarSiguiente()

      await fetchTurno()

      setMessage(
        'El cliente ha sido notificado en su dispositivo móvil'
      )
    } catch {
      await fetchTurno()
      setMessage(
        'No se pudo llamar al cliente. Intenta nuevamente.'
      )
    } finally {
      setBusy(false)
    }
  }

  const handleFinish = async () => {
    if (!turno) return

    setBusy(true)
    try {
      const turnoFinalizadoId = turno.turnoId
      await queueService.finalizarTurno(turnoFinalizadoId)
      setTurno(null)
      setTurnoLlamado(false)
      setMessage('Turno marcado como atendido. Buscando el siguiente cliente...')
      await fetchTurno({ ignoredTurnoId: turnoFinalizadoId })
    } catch {
      await fetchTurno()
      setMessage('No se pudo finalizar el turno.')
    } finally {
      setBusy(false)
    }
  }

  const handleNoShow = async () => {
    if (!turno) return

    setBusy(true)
    try {
      const turnoAusenteId = turno.turnoId
      await queueService.marcarNoPresentado(turnoAusenteId)
      setTurno(null)
      setTurnoLlamado(false)
      await fetchTurno({ ignoredTurnoId: turnoAusenteId })
    } catch {
      await fetchTurno()
      setMessage('No se pudo marcar el turno como no presentado.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="llamar-page">
      <BackButton to="/admin/fila" />
      <StatsButton />
      <header className="llamar-header">
        <h2>Llamar al siguiente cliente</h2>
      </header>

      <div className="llamar-card">
        <div className="bell-icon">🔔</div>
        <h3>Turno actual</h3>
        {turno && (
          <p className="llamar-nombre">{turno.nombreCliente || 'Cliente anónimo'}</p>
        )}
        <div className="llamar-turno">
          {loading ? '...' : turno ? `#${turno.numeroTurno}` : '—'}
        </div>
        <p className="llamar-people">
          {turno ? `${turno.cantidadIntegrantes ?? 1} personas` : 'Sin turno disponible'}
        </p>
        <p className="llamar-note">El cliente será notificado en su dispositivo móvil.</p>
      </div>

      {turnoLlamado ? (
        <>
          <button className="llamar-button primary" type="button" onClick={handleFinish} disabled={busy || loading || !turno}>
            {busy ? 'Procesando...' : 'Marcar como atendido'}
          </button>
          <button className="llamar-button secondary" type="button" onClick={handleNoShow} disabled={busy || loading || !turno}>
            No se presentó
          </button>
        </>
      ) : (
        <button className="llamar-button primary" type="button" onClick={handleCall} disabled={busy || loading || !turno}>
          {busy ? 'Procesando...' : 'Llamar cliente'}
        </button>
      )}

      <p className="llamar-message">{message}</p>
    </div>
  )
}

export default LlamarCliente
