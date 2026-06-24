import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import logoDigitalQueue from '../../shared/assets/logo.jpeg'
import publicTurnoService from '../../shared/services/publicTurnoService'
import { obtenerUltimoLocal } from '../utils/ultimoLocal'
import { guardarTurnoActivo, obtenerTurnoActivo } from '../utils/sesionTurno'
import { obtenerTokenTurnoDesdeUrl } from '../utils/turnoLink'

function PortalCliente() {
  const ultimoLocal = obtenerUltimoLocal()
  const tokenTurnoUrl = obtenerTokenTurnoDesdeUrl()
  const turnoActivo = obtenerTurnoActivo()
  const [tokenRecuperado, setTokenRecuperado] = useState(null)

  useEffect(() => {
    if (!tokenTurnoUrl) return
    guardarTurnoActivo(tokenTurnoUrl)
  }, [tokenTurnoUrl])

  useEffect(() => {
    if (turnoActivo?.tokenPublico || tokenTurnoUrl) return
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) return

    const recuperarTurno = async () => {
      try {
        const registration = await navigator.serviceWorker.getRegistration()
        const subscription = await registration?.pushManager.getSubscription()
        if (!subscription) return

        const data = await publicTurnoService.recuperarTurnoActivo(subscription)
        setTokenRecuperado(data.tokenPublico)
      } catch {
        // No tener un turno asociado es un estado normal del portal público.
      }
    }

    recuperarTurno()
  }, [turnoActivo?.tokenPublico, tokenTurnoUrl])

  const tokenPublico = tokenTurnoUrl || turnoActivo?.tokenPublico || tokenRecuperado
  if (tokenPublico) {
    return <Navigate to={`/turno/${tokenPublico}`} replace />
  }

  return (
    <main className="pantalla portal-cliente">
      <img
        src={logoDigitalQueue}
        alt="Digital Queue"
        className="portal-cliente-logo"
      />

      <span className="portal-cliente-etiqueta">Tu fila, sin esperar de más</span>
      <h1>Digital Queue</h1>
      <p className="portal-cliente-descripcion">
        Escaneá el QR del restaurante para ingresar a su fila y seguir tu turno.
      </p>

      {ultimoLocal?.codigoPublico && (
        <Link
          className="portal-cliente-accion"
          to={`/fila/${ultimoLocal.codigoPublico}`}
        >
          Volver a {ultimoLocal.nombreLocal || 'mi último restaurante'}
        </Link>
      )}

      <section className="portal-cliente-ayuda">
        <strong>¿Vas a otro lugar?</strong>
        <p>Escaneá su QR. El enlace abrirá directamente la fila de ese local.</p>
      </section>
    </main>
  )
}

export default PortalCliente
