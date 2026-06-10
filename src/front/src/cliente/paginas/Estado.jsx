import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import logoElAntojo from '../assets/el-antojo.svg'
import BarraProgreso from '../componentes/BarraProgreso'
import TarjetaEstado from '../componentes/TarjetaEstado'
import publicTurnoService from '../../shared/services/publicTurnoService'

const calcularProgreso = (estado, personasAdelante) => {
  if (estado === 'LLAMADO' || personasAdelante === 0) return 100
  if (personasAdelante <= 2) return 75
  if (personasAdelante <= 5) return 45
  return 20
}

function Estado() {
  const { tokenPublico } = useParams()
  const [turno, setTurno] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

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

  const personasAdelante = turno?.personasAdelante ?? 0
  const estado = turno?.estado ?? 'ESPERANDO'
  const progreso = calcularProgreso(estado, personasAdelante)

  return (
    <main className="pantalla">
      <section className="marca marca-estado">
        <img
          src={logoElAntojo}
          alt="El Antojo"
          className="logo-local"
        />
      </section>

      <BarraProgreso porcentaje={loading ? 15 : progreso} />

      <section className="grilla-estado">
        <TarjetaEstado
          titulo="Hora En Que Debe Presentarse"
          valor={estado === 'LLAMADO' ? 'Ahora' : 'Próximamente'}
        />

        <TarjetaEstado
          titulo="Tiempo De Espera"
          valor={`${turno?.tiempoEstimadoMinutos ?? 0} minutos`}
        />

        <TarjetaEstado
          titulo="Mi Número"
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
          <input type="checkbox" />
          <span></span>
        </label>
      </section>

      <footer className="logo-dq">DQ</footer>
    </main>
  )
}

export default Estado
