import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import logoElAntojo from '../assets/starbucks.svg'
import BarraProgreso from '../componentes/BarraProgreso'
import TarjetaEstado from '../componentes/TarjetaEstado'
import publicTurnoService from '../../shared/services/publicTurnoService'
const calcularProgreso = (estado, personasAdelante) => {
  if (estado === 'LLAMADO' || personasAdelante === 0) return 100
  if (personasAdelante <= 2) return 75
  if (personasAdelante <= 5) return 45
  return 20
}

const CONFIG_ESTADO = {
  ESPERANDO: {
    icono: '🔔',
    titulo: 'Tu turno se acerca',
    mensaje: 'Faltan varias personas antes que vos.'
  },

  PROXIMO: {
    icono: '🔔',
    titulo: '¡Estás próximo!',
    mensaje: 'Falta muy poco para tu turno.'
  },

  LLAMADO: {
    icono: '🔔',
    titulo: '¡Es tu turno!',
    mensaje: 'Presentate en el mostrador para ser atendido.'
  },

  FINALIZADO: {
    icono: '✅',
    titulo: 'Gracias por su tiempo',
    mensaje: 'Tu atención fue completada con éxito.'
  },

  NO_PRESENTADO: {
    icono: '❌',
    titulo: 'Perdiste tu lugar',
    mensaje: 'No te presentaste a tiempo.'
  },

  CANCELADO: {
    icono: '❌',
    titulo: 'Turno cancelado',
    mensaje: 'Podés volver a anotarte cuando quieras.'
  }
}
const urlBase64ToUint8Array = (base64String) => {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4)

  const base64 = (base64String + padding)
    .replace(/-/g, '+')
    .replace(/_/g, '/')

  const rawData = window.atob(base64)

  return Uint8Array.from(
    [...rawData].map((char) => char.charCodeAt(0)),
  )
}
function Estado() {
  const { tokenPublico } = useParams()
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notificacionesActivas, setNotificacionesActivas] = useState(false)

  useEffect(() => {
    const fetchTurno = async () => {
      setLoading(true)
      setError(null)

      try {
        const data = await publicTurnoService.getEstado(tokenPublico)
        setTurno(data)
      } catch {
        setError('No se pudo cargar tu turno.')
      } finally {
        setLoading(false)
      }
    }

    fetchTurno()
    const interval = window.setInterval(fetchTurno, 15000)
    return () => window.clearInterval(interval)
  }, [tokenPublico])
  const calcularHoraEstimada = () => {
    const minutos = turno?.tiempoEstimadoMinutos ?? 0

    const fecha = new Date()
    fecha.setMinutes(fecha.getMinutes() + minutos)

    return fecha.toLocaleTimeString('es-AR', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  const activarNotificaciones = async () => {
    try {
      const permiso = await Notification.requestPermission()

      if (permiso !== 'granted') {
        alert('Debés permitir las notificaciones.')
        return
      }

      const registration =
        await navigator.serviceWorker.register(
          '/service-worker.js'
        )

      const publicKey =
        await publicTurnoService.getPushPublicKey()

      const subscription =
        await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey:
            urlBase64ToUint8Array(publicKey),
        })

      await publicTurnoService.registrarPushSubscription(
        tokenPublico,
        subscription,
      )

      setNotificacionesActivas(true)

      alert('Notificaciones activadas.')
    } catch (error) {
      console.error(error)
      alert('No se pudieron activar las notificaciones.')
    }
  }



  const personasAdelante = turno?.personasAdelante ?? 0
  const estado = turno?.estado ?? 'ESPERANDO'
  const estadoConfig =
    CONFIG_ESTADO[estado] || CONFIG_ESTADO.ESPERANDO

  const progreso = calcularProgreso(estado, personasAdelante)
  const nombreCliente =
    turno?.nombreCliente
      ? turno.nombreCliente.charAt(0).toUpperCase() +
      turno.nombreCliente.slice(1)
      : 'Cliente'
  const handleCancelarTurno = async () => {
    const confirmar = window.confirm(
      '¿Estás seguro de que querés salir de la fila?'
    )

    if (!confirmar) return

    try {
      await publicTurnoService.cancelar(tokenPublico)

      alert('Tu turno fue cancelado.')

      window.location.href = '/fila/starbucks-uade'
    } catch {
      alert('No se pudo cancelar el turno.')
    }
  }

  return (
    <main className="pantalla">
      <section className="marca marca-estado">
        <img
          src={logoElAntojo}
          alt="El Antojo"
          className="logo-local"
        />
      </section>

      <section className="estado-banner">
        <div className="estado-icono">
          {estadoConfig.icono}
        </div>

        <h3 className="estado-titulo">
          {estadoConfig.titulo}
        </h3>

        <p className="estado-mensaje">
          {estadoConfig.mensaje}
        </p>
      </section>
      <h2 className="cliente-saludo">
        ¡Hola, {nombreCliente}!
      </h2>
      <BarraProgreso porcentaje={loading ? 15 : progreso} />

      <section className="grilla-estado">
        <TarjetaEstado
          titulo="Hora estimada"
          valor={
            estado === 'LLAMADO'
              ? 'Ahora'
              : calcularHoraEstimada()
          }
        />

        <TarjetaEstado
          titulo="Tiempo De Espera"
          valor={`${turno?.tiempoEstimadoMinutos ?? 0} minutos`}
        />

        <TarjetaEstado
          titulo="Turno"
          valor={loading ? '...' : String(turno?.numeroTurno ?? '-')}
        />

        <TarjetaEstado
          titulo="Puesto En Fila"
          valor={loading ? '...' : String(personasAdelante)}
        />
      </section>

      {error && <p className="cliente-error">{error}</p>}

      <section className="notificacion">
        <div>
          <h3>Notificación de turno</h3>
          <p>
            {personasAdelante <= 2
              ? 'Ya casi es tu turno. Tenés pocas personas por delante.'
              : 'Active si quiere que le avisemos su turno'}
          </p>
        </div>

        <label className="switch">

          <input
            type="checkbox"
            checked={notificacionesActivas}
            onChange={activarNotificaciones}
          />
          <span></span>
        </label>
      </section>
      <button
        className="boton-salir-fila"
        onClick={handleCancelarTurno}
      >
        Salir de la fila
      </button>

      <footer className="logo-dq">DQ</footer>
    </main>
  )
}

export default Estado
