import { useMemo, useState } from 'react'
import BackButton from '../componentes/BackButton'
import '../estilos/DemoNotificacion.css'

const PROXIMO_MENSAJE = 'Ya casi es tu turno. Tenés 2 personas por delante.'
const TURNO_MENSAJE = 'Es tu turno. Acercate al punto de atención.'

function DemoNotificacion() {
  const [personasDelante, setPersonasDelante] = useState(4)
  const [notification, setNotification] = useState(null)
  const [notificationKey, setNotificationKey] = useState(0)

  const estado = useMemo(() => {
    if (personasDelante <= 0) return 'LLAMADO'
    if (personasDelante <= 2) return 'PROXIMO'
    return 'ESPERANDO'
  }, [personasDelante])

  const showNotification = (message) => {
    setNotification({ title: 'Digital Queue', message })
    setNotificationKey((current) => current + 1)
  }

  const handleTestNotification = () => {
    showNotification(personasDelante <= 0 ? TURNO_MENSAJE : PROXIMO_MENSAJE)
  }

  const handleAdvance = () => {
    setPersonasDelante((current) => {
      const next = Math.max(0, current - 1)
      if (next === 2) showNotification(PROXIMO_MENSAJE)
      if (next === 0) showNotification(TURNO_MENSAJE)
      return next
    })
  }

  const handleReset = () => {
    setPersonasDelante(4)
    setNotification(null)
  }

  return (
    <div className="demo-page">
      <BackButton to="/admin/dashboard" />

      <header className="demo-header">
        <div>
          <span className="demo-label">Demo cliente</span>
          <h2>Notificaciones de turno</h2>
          <p>Vista responsive del aviso que recibe el cliente en su celular.</p>
        </div>
      </header>

      <section className="demo-content">
        <div className="demo-controls">
          <div className="demo-status">
            <span>Estado</span>
            <strong>{estado}</strong>
          </div>

          <div className="demo-position">
            <span>Personas delante</span>
            <strong>{personasDelante}</strong>
          </div>

          <button type="button" className="demo-primary" onClick={handleTestNotification}>
            Enviar notificación de prueba
          </button>

          <button type="button" className="demo-secondary" onClick={handleAdvance}>
            Avanzar posición
          </button>

          <button type="button" className="demo-ghost" onClick={handleReset}>
            Reiniciar demo
          </button>
        </div>

        <div className="phone-shell" aria-label="Vista de celular del cliente">
          <div className="phone-speaker" />
          <div className="phone-screen">
            <div className="phone-topbar">
              <span>9:41</span>
              <span>5G 82%</span>
            </div>

            <div className="phone-app">
              <div className="phone-brand">DQ</div>
              <p>Tu fila virtual</p>
              <strong>Turno #23</strong>
              <span className={`phone-state ${estado.toLowerCase()}`}>{estado}</span>
              <div className="phone-count">
                <span>Personas delante</span>
                <strong>{personasDelante}</strong>
              </div>
            </div>

            {notification && (
              <div key={notificationKey} className="mobile-notification">
                <div className="notification-icon">DQ</div>
                <div>
                  <strong>{notification.title}</strong>
                  <p>{notification.message}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  )
}

export default DemoNotificacion
