import { useEffect, useState } from 'react'
import queueService from '../../shared/services/queueService'
import BackButton from '../componentes/BackButton'
import StatsButton from '../componentes/StatsButton'
import '../estilos/LlamarCliente.css'

function LlamarCliente() {
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [turnoLlamado, setTurnoLlamado] = useState(false)
  const [message, setMessage] = useState('Cargando el siguiente cliente...')

  const fetchTurno = async () => {
    setLoading(true)
    try {
      const turnos = await queueService.getTurnos()
      const enAtencion = turnos.find((item) => item.estado === 'LLAMADO' || item.estado === 'ATENDIENDO')
      const siguiente = turnos.find((item) => item.estado === 'ESPERANDO' || item.estado === 'PROXIMO')
      const turnoActual = enAtencion || siguiente
      if (turnoActual) {
        setTurno(turnoActual)
        setTurnoLlamado(Boolean(enAtencion))
        setMessage(enAtencion
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
      setLoading(false)
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(fetchTurno, 0)
    const interval = window.setInterval(fetchTurno, 10000)
    return () => {
      window.clearTimeout(timer)
      window.clearInterval(interval)
    }
  }, [])

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
      await queueService.finalizarTurno(turno.turnoId)
      await fetchTurno()
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
      await queueService.marcarNoPresentado(turno.turnoId)
      await fetchTurno()
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
