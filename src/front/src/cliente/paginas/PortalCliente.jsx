import { Link } from 'react-router-dom'
import logoDigitalQueue from '../../shared/assets/logo.jpeg'
import { obtenerUltimoLocal } from '../utils/ultimoLocal'

function PortalCliente() {
  const ultimoLocal = obtenerUltimoLocal()

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
