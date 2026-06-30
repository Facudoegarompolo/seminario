import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Clock3, Timer } from 'lucide-react'
import logoElAntojo from '../assets/starbucks.svg'
import BarraProgreso from '../componentes/BarraProgreso'
import TarjetaEstado from '../componentes/TarjetaEstado'
import publicTurnoService from '../../shared/services/publicTurnoService'
import { guardarUltimoLocal } from '../utils/ultimoLocal'
import { guardarTurnoActivo, limpiarTurnoActivo } from '../utils/sesionTurno'
import { fijarUrlInstalacionTurno, prepararManifestTurno } from '../utils/manifestTurno'

const calcularProgreso = (estado, personasAdelante) => {
  if (estado === 'LLAMADO' || personasAdelante === 0) return 100
  if (personasAdelante <= 2) return 75
  if (personasAdelante <= 5) return 45
  return 20
}

const CONFIG_ESTADO = {
  ESPERANDO: {
    icono: <Clock3 size={32} strokeWidth={2.4} />,
    titulo: 'Tu turno se acerca',
    mensaje: 'Hay otras personas antes que vos.'
  },

  PROXIMO: {
    icono: <Timer size={32} strokeWidth={2.4} />,
    titulo: '¡Estás próximo!',
    mensaje: 'Estate atento: falta poco para que te llamen.'
  },

  LLAMADO: {
    icono: '🔔',
    titulo: '¡Es tu turno!',
    mensaje: 'Acercate al mostrador para que podamos atenderte.'
  },

  FINALIZADO: {
    icono: '✅',
    titulo: 'Gracias por tu tiempo',
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
  },

  EXPIRADO: {
    icono: '❌',
    titulo: 'Turno vencido',
    mensaje: 'Este turno ya no se encuentra activo.'
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

const sonClavesIguales = (primera, segunda) => {
  if (!primera || primera.byteLength !== segunda.byteLength) return false

  const bytesPrimera = new Uint8Array(primera)
  return bytesPrimera.every((byte, index) => byte === segunda[index])
}

const esDispositivoIOS = () =>
  /iphone|ipad|ipod/i.test(window.navigator.userAgent) ||
  (window.navigator.platform === 'MacIntel' &&
    window.navigator.maxTouchPoints > 1)

const esAplicacionInstalada = () =>
  window.navigator.standalone === true ||
  window.matchMedia('(display-mode: standalone)').matches

function Estado() {
  const { tokenPublico } = useParams()
  const navigate = useNavigate()
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notificacionesActivas, setNotificacionesActivas] = useState(false)
  const [prioridadSolicitada, setPrioridadSolicitada] = useState(false)
  const [requiereInstalacion] = useState(
    () => esDispositivoIOS() && !esAplicacionInstalada(),
  )
  const [mostrarGuiaInstalacion, setMostrarGuiaInstalacion] = useState(false)

  useEffect(() => {
    if (!tokenPublico) return

    prepararManifestTurno(tokenPublico).catch(() => {
      // La URL /turno/:token sigue siendo la recuperación principal si falla el manifest dinámico.
    })
  }, [tokenPublico])

  useEffect(() => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) return

    let efectoActivo = true

    const sincronizarSuscripcion = async () => {
      try {
        const registration = await navigator.serviceWorker.getRegistration()
        const subscription = await registration?.pushManager.getSubscription()

        if (!subscription) {
          if (efectoActivo) setNotificacionesActivas(false)
          return
        }

        await publicTurnoService.registrarPushSubscription(tokenPublico, subscription)

        if (efectoActivo) setNotificacionesActivas(true)
      } catch {
        if (efectoActivo) setNotificacionesActivas(false)
      }
    }

    sincronizarSuscripcion()

    return () => {
      efectoActivo = false
    }
  }, [tokenPublico])

  useEffect(() => {
    const fetchTurno = async () => {
      setLoading(true)
      setError(null)

      try {
        const data = await publicTurnoService.getEstado(tokenPublico)
        setTurno(data)

        if (data?.prioridad) {
          setPrioridadSolicitada(true)
        }

        guardarUltimoLocal(data.codigoPublico, data.nombreLocal)
        guardarTurnoActivo(tokenPublico, data)
      } catch (err) {
        if (err.response?.status === 404) {
          limpiarTurnoActivo(tokenPublico)
        }

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
      const esIOS = esDispositivoIOS()
      const esPWA = esAplicacionInstalada()

      if (!window.isSecureContext) {
        alert('Las notificaciones requieren abrir la web mediante HTTPS.')
        return
      }

      if (esIOS && !esPWA) {
        fijarUrlInstalacionTurno(tokenPublico)
        await prepararManifestTurno(tokenPublico)
        setMostrarGuiaInstalacion(true)
        return
      }

      if (!('Notification' in window)) {
        alert('Este navegador no soporta notificaciones web.')
        return
      }

      if (!('serviceWorker' in navigator)) {
        alert('Este navegador no soporta service workers.')
        return
      }

      if (!('PushManager' in window)) {
        alert('Este navegador no soporta notificaciones push.')
        return
      }

      const permiso = await window.Notification.requestPermission()

      if (permiso !== 'granted') {
        alert('Debés permitir las notificaciones.')
        return
      }

      await navigator.serviceWorker.register('/service-worker.js')
      const registration = await navigator.serviceWorker.ready

      const publicKey = await publicTurnoService.getPushPublicKey()

      if (!publicKey) {
        throw new Error('El servidor no tiene configurada la clave publica VAPID.')
      }

      const applicationServerKey = urlBase64ToUint8Array(publicKey)

      let existingSubscription =
        await registration.pushManager.getSubscription()

      if (
        existingSubscription &&
        !sonClavesIguales(
          existingSubscription.options?.applicationServerKey,
          applicationServerKey,
        )
      ) {
        await existingSubscription.unsubscribe()
        existingSubscription = null
      }

      const subscription =
        existingSubscription ||
        await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey,
        })

      await publicTurnoService.registrarPushSubscription(
        tokenPublico,
        subscription,
      )

      setNotificacionesActivas(true)

      alert('Notificaciones activadas. Te avisaremos cuando falte poco para tu turno.')
    } catch (error) {
      console.error('ERROR PUSH:', error)

      alert(
        JSON.stringify({
          name: error?.name,
          message: error?.message,
        })
      )
    }
  }

  const desactivarNotificaciones = async () => {
    try {
      const registration = await navigator.serviceWorker.getRegistration()
      const subscription = await registration?.pushManager.getSubscription()

      if (subscription) {
        await publicTurnoService.desactivarPushSubscription(tokenPublico, subscription)
        await subscription.unsubscribe()
      }

      setNotificacionesActivas(false)
    } catch {
      alert('No se pudieron desactivar las notificaciones.')
    }
  }

  const cambiarNotificaciones = (event) => {
    if (event.target.checked) {
      activarNotificaciones()
    } else {
      desactivarNotificaciones()
    }
  }

  const handleSolicitarPrioridad = async () => {
    try {
      const turnoActualizado = await publicTurnoService.solicitarPrioridad(tokenPublico)

      if (turnoActualizado) {
        setTurno(turnoActualizado)
      }

      setPrioridadSolicitada(true)
    } catch {
      alert('No se pudo solicitar atención con prioridad.')
    }
  }

  const handleCancelarTurno = async () => {
    const confirmar = window.confirm(
      '¿Estás seguro de que querés salir de la fila?'
    )

    if (!confirmar) return

    try {
      const turnoCancelado = await publicTurnoService.cancelar(tokenPublico)

      if (turnoCancelado) {
        setTurno(turnoCancelado)
        limpiarTurnoActivo(tokenPublico)
        guardarUltimoLocal(
          turnoCancelado.codigoPublico,
          turnoCancelado.nombreLocal,
        )
      } else {
        setTurno((turnoActual) => ({
          ...turnoActual,
          estado: 'CANCELADO'
        }))
      }
    } catch {
      alert('No se pudo cancelar el turno.')
    }
  }

  const volverAlRestaurante = () => {
    if (turno?.codigoPublico) {
      navigate(`/fila/${turno.codigoPublico}`)
      return
    }

    navigate('/')
  }

  const personasAdelante = turno?.personasAdelante ?? 0
  const estado = turno?.estado ?? 'ESPERANDO'
  const estadoConfig = CONFIG_ESTADO[estado] || CONFIG_ESTADO.ESPERANDO
  const progreso = calcularProgreso(estado, personasAdelante)
  const puedeCancelar = ['ESPERANDO', 'PROXIMO', 'LLAMADO'].includes(estado)
  const puedeSolicitarPrioridad = ['ESPERANDO', 'PROXIMO'].includes(estado)
  const turnoTerminado = [
    'FINALIZADO',
    'NO_PRESENTADO',
    'CANCELADO',
    'EXPIRADO',
  ].includes(estado)

  const nombreCliente =
    turno?.nombreCliente
      ? turno.nombreCliente.charAt(0).toUpperCase() +
      turno.nombreCliente.slice(1)
      : 'Cliente'

  if (!loading && error && !turno) {
    return (
      <main className="pantalla portal-cliente">
        <span className="portal-cliente-etiqueta">Digital Queue</span>
        <h1>Este turno ya no está disponible</h1>
        <p className="portal-cliente-descripcion">
          Podés escanear el QR del restaurante para ingresar a una nueva fila.
        </p>
        <button
          type="button"
          className="portal-cliente-accion"
          onClick={() => navigate('/')}
        >
          Ir al inicio
        </button>
      </main>
    )
  }

  if (estado === 'LLAMADO') {
    return (
      <main className="pantalla">
        <section className="marca marca-estado">
          <img
            src={logoElAntojo}
            alt="El Antojo"
            className="logo-local"
          />
        </section>

        <section className="pantalla-turno-llamado">
          <div className="icono-turno-llamado">
            ✓
          </div>

          <p className="turno-llamado-saludo">
            {nombreCliente}
          </p>

          <h1>¡Es tu turno!</h1>

          <p className="turno-llamado-mensaje">
            Acercate al mostrador para que podamos atenderte.
          </p>

          <p className="turno-llamado-numero">
            Turno #{turno?.numeroTurno ?? '-'}
          </p>
        </section>

        <footer className="logo-dq">DQ</footer>
      </main>
    )
  }

  if (estado === 'CANCELADO') {
    return (
      <main className="pantalla">
        <section className="marca marca-estado">
          <img
            src={logoElAntojo}
            alt="El Antojo"
            className="logo-local"
          />
        </section>

        <section className="pantalla-turno-final">
          <div className="tarjeta-estado-final">
            <div className="icono-turno-final icono-turno-cancelado">
              ×
            </div>

            <p className="turno-llamado-saludo">
              {nombreCliente}
            </p>

            <h1>Saliste de la fila</h1>

            <p className="turno-llamado-mensaje mensaje-cancelado">
              Tu turno fue cancelado correctamente.
            </p>

            <p className="turno-llamado-numero">
              Turno #{turno?.numeroTurno ?? '-'}
            </p>

            <button
              className="boton-volver-fila"
              type="button"
              onClick={volverAlRestaurante}
            >
              Volver a anotarme
            </button>
          </div>
        </section>

        <footer className="logo-dq">DQ</footer>
      </main>
    )
  }

  if (estado === 'FINALIZADO') {
    return (
      <main className="pantalla">
        <section className="marca marca-estado">
          <img
            src={logoElAntojo}
            alt="El Antojo"
            className="logo-local"
          />
        </section>

        <section className="pantalla-turno-final">
          <div className="icono-turno-final icono-turno-finalizado">
            ✓
          </div>

          <p className="turno-llamado-saludo">
            {nombreCliente}
          </p>

          <h1>Gracias por tu visita</h1>

          <p className="turno-llamado-mensaje mensaje-finalizado">
            Tu atención fue completada con éxito.
          </p>

          <p className="turno-llamado-numero">
            Turno #{turno?.numeroTurno ?? '-'}
          </p>

          <button
            className="boton-volver-fila"
            type="button"
            onClick={volverAlRestaurante}
          >
            Volver a Starbucks UADE
          </button>
        </section>

        <footer className="logo-dq">DQ</footer>
      </main>
    )
  }

  if (estado === 'NO_PRESENTADO') {
    return (
      <main className="pantalla">
        <section className="marca marca-estado">
          <img
            src={logoElAntojo}
            alt="El Antojo"
            className="logo-local"
          />
        </section>

        <section className="pantalla-turno-final">
          <div className="icono-turno-final icono-turno-no-presentado">
            !
          </div>

          <p className="turno-llamado-saludo">
            {nombreCliente}
          </p>

          <h1>No te presentaste</h1>

          <p className="turno-llamado-mensaje mensaje-no-presentado">
            Tu turno fue marcado como no presentado.
          </p>

          <p className="turno-llamado-numero">
            Turno #{turno?.numeroTurno ?? '-'}
          </p>

          <button
            className="boton-volver-fila"
            type="button"
            onClick={volverAlRestaurante}
          >
            Volver a anotarme
          </button>
        </section>

        <footer className="logo-dq">DQ</footer>
      </main>
    )
  }

  if (prioridadSolicitada) {
    return (
      <main className="pantalla">
        <section className="marca marca-estado">
          <img
            src={logoElAntojo}
            alt="El Antojo"
            className="logo-local"
          />
        </section>

        <section className="pantalla-turno-final">
          <div className="icono-turno-final icono-turno-prioridad">
            !
          </div>

          <p className="turno-llamado-saludo">
            {nombreCliente}
          </p>

          <h1>Solicitud recibida</h1>

          <p className="turno-llamado-mensaje mensaje-prioridad">
            Esperá un momento, el encargado del local se acercará para atenderte.
          </p>

          <p className="turno-llamado-numero">
            Turno #{turno?.numeroTurno ?? '-'}
          </p>
        </section>

        <footer className="logo-dq">DQ</footer>
      </main>
    )
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
          valor={calcularHoraEstimada()}
        />

        <TarjetaEstado
          titulo="Tiempo de espera"
          valor={`${turno?.tiempoEstimadoMinutos ?? 0} minutos`}
        />

        <TarjetaEstado
          titulo="Turno"
          valor={loading ? '...' : String(turno?.numeroTurno ?? '-')}
        />

        <TarjetaEstado
          titulo="Puesto en fila"
          valor={loading ? '...' : String(personasAdelante)}
        />
      </section>

      {error && <p className="cliente-error">{error}</p>}

      {puedeSolicitarPrioridad && (
        <section className="prioridad-cliente">
          <button
            className="link-prioridad"
            type="button"
            onClick={handleSolicitarPrioridad}
          >
            Necesito atención con prioridad
          </button>
        </section>
      )}

      {!turnoTerminado && (
        <section className="notificacion">
          <div>
            <h3>🔔 Notificación de turno</h3>
          </div>

          {requiereInstalacion ? (
            <button
              type="button"
              className="boton-activar-avisos"
              onClick={() => setMostrarGuiaInstalacion(true)}
            >
              Activar avisos
            </button>
          ) : (
            <label className="switch">
              <input
                type="checkbox"
                checked={notificacionesActivas}
                onChange={cambiarNotificaciones}
              />
              <span></span>
            </label>
          )}
        </section>
      )}

      {puedeCancelar && (
        <button
          className="boton-salir-fila"
          onClick={handleCancelarTurno}
        >
          {estado === 'LLAMADO' ? 'Cancelar mi turno' : 'Salir de la fila'}
        </button>
      )}

      {turnoTerminado && (
        <section className="acciones-turno-terminado">
          <h3>¿Qué querés hacer ahora?</h3>
          <button type="button" onClick={volverAlRestaurante}>
            Volver a {turno?.nombreLocal || 'este restaurante'}
          </button>
          <button
            type="button"
            className="accion-secundaria"
            onClick={() => navigate('/')}
          >
            Ir al inicio de Digital Queue
          </button>
        </section>
      )}

      <footer className="logo-dq">DQ</footer>

      {mostrarGuiaInstalacion && (
        <div
          className="guia-instalacion-fondo"
          role="presentation"
          onClick={() => setMostrarGuiaInstalacion(false)}
        >
          <section
            className="guia-instalacion"
            role="dialog"
            aria-modal="true"
            aria-labelledby="titulo-guia-instalacion"
            onClick={(event) => event.stopPropagation()}
          >
            <button
              type="button"
              className="guia-instalacion-cerrar"
              aria-label="Cerrar instrucciones"
              onClick={() => setMostrarGuiaInstalacion(false)}
            >
              ×
            </button>

            <span className="guia-instalacion-etiqueta">iPhone y iPad</span>

            <h2 id="titulo-guia-instalacion">
              Recibí el aviso con Safari cerrado
            </h2>

            <p className="guia-instalacion-intro">
              No necesitás App Store ni una cuenta. Agregá Digital Queue a tu
              pantalla de inicio una sola vez.
            </p>

            <ol className="guia-instalacion-pasos">
              <li>
                <span>1</span>
                <p>Tocá <strong>Compartir</strong> en la barra de Safari.</p>
              </li>

              <li>
                <span>2</span>
                <p>Elegí <strong>Agregar a inicio</strong> y confirmá.</p>
              </li>

              <li>
                <span>3</span>
                <p>Abrí <strong>Digital Queue</strong> desde el nuevo ícono.</p>
              </li>

              <li>
                <span>4</span>
                <p>Activá los avisos cuando vuelvas a ver tu turno.</p>
              </li>
            </ol>

            <p className="guia-instalacion-nota">
              El ícono abrirá directamente este turno. Si la app abre el inicio,
              Digital Queue recuperará tu turno automáticamente.
            </p>

            <button
              type="button"
              className="guia-instalacion-listo"
              onClick={() => setMostrarGuiaInstalacion(false)}
            >
              Entendido
            </button>
          </section>
        </div>
      )}
    </main>
  )
}

export default Estado