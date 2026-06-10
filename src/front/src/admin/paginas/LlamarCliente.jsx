import { useEffect, useState } from 'react'
import queueService from '../../shared/services/queueService'
import BackButton from '../componentes/BackButton'
import StatsButton from '../componentes/StatsButton'
import '../estilos/LlamarCliente.css'

function LlamarCliente() {
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('Cargando el siguiente cliente...')

  const fetchTurno = async () => {
    setLoading(true)
    try {
      const turnos = await queueService.getTurnos()
      const siguiente = turnos.find((item) => item.estado === 'ESPERANDO' || item.estado === 'PROXIMO')
      if (siguiente) {
        setTurno(siguiente)
        setMessage('Listo para llamar al siguiente cliente')
      } else {
        setTurno(null)
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
    return () => window.clearTimeout(timer)
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
      setMessage(
        'No se pudo llamar al cliente. Intenta nuevamente.'
      )
    } finally {
      setBusy(false)
    }
  }

  const handleSkip = async () => {
    if (!turno) return

    setBusy(true)
    try {
      await queueService.marcarNoPresentado(turno.turnoId)
      await fetchTurno()
    } catch {
      setMessage('No se pudo saltar al siguiente cliente.')
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
        <div className="llamar-turno">
          {loading ? '...' : turno ? `#${turno.numeroTurno}` : '—'}
        </div>
        <p className="llamar-people">
          {turno ? `${turno.cantidadIntegrantes ?? 1} personas` : 'Sin turno disponible'}
        </p>
        <p className="llamar-note">El cliente será notificado en su dispositivo móvil.</p>
      </div>

      <button className="llamar-button primary" type="button" onClick={handleCall} disabled={busy || loading || !turno}>
        {busy ? 'Procesando...' : 'Llamar cliente'}
      </button>
      <button className="llamar-button secondary" type="button" onClick={handleSkip} disabled={busy || loading || !turno}>
        Saltar
      </button>

      <p className="llamar-message">{message}</p>
    </div>
  )
}

export default LlamarCliente
