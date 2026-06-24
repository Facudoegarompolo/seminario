import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import TarjetaInfo from '../componentes/TarjetaInfo'
import BotonPrincipal from '../componentes/BotonPrincipal'
import logoElAntojo from '../assets/starbucks.svg'
import publicFilaService from '../../shared/services/publicFilaService'
import { guardarUltimoLocal } from '../utils/ultimoLocal'
import { guardarTurnoActivo, obtenerTurnoActivo } from '../utils/sesionTurno'
import { obtenerTokenTurnoDesdeUrl } from '../utils/turnoLink'

function Inicio() {
  const navigate = useNavigate()
  const { codigoPublico = 'starbucks-uade' } = useParams()
  const tokenTurnoUrl = obtenerTokenTurnoDesdeUrl()
  const turnoActivo = obtenerTurnoActivo()
  const [nombreCliente, setNombreCliente] = useState('')
  const [fila, setFila] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    const tokenPublico = tokenTurnoUrl || turnoActivo?.tokenPublico
    if (!tokenPublico) return

    guardarTurnoActivo(tokenPublico, turnoActivo || {})
    navigate(`/turno/${tokenPublico}`, { replace: true })
  }, [navigate, tokenTurnoUrl, turnoActivo])

  useEffect(() => {
    const fetchFila = async () => {
      setLoading(true)
      setError(null)

      try {
        const data = await publicFilaService.getEstado(codigoPublico)
        setFila(data)
        guardarUltimoLocal(codigoPublico, data.nombreLocal)
      } catch {
        setError('No se pudo cargar la fila.')
      } finally {
        setLoading(false)
      }
    }

    fetchFila()
  }, [codigoPublico])

  const handleSubmit = async () => {
    setSubmitting(true)
    setError(null)

    try {
      const turno = await publicFilaService.crearTurno(codigoPublico, {
        nombreCliente: nombreCliente.trim() || undefined,
        cantidadIntegrantes: 1,
      })
      guardarTurnoActivo(turno.tokenPublico, {
        ...turno,
        codigoPublico,
        nombreLocal: fila?.nombreLocal,
      })
      navigate(`/turno/${turno.tokenPublico}`)
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo crear el turno.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="pantalla">
      <section className="marca">
        <img
          src={logoElAntojo}
          alt={fila?.nombreLocal || 'El Antojo'}
          className="logo-local"
        />
      </section>

      <section className="seccion-info">
        <TarjetaInfo
          titulo="Gente En Fila"
          valor={loading ? '...' : String(fila?.personasEsperando ?? 0)}
        />

        <TarjetaInfo
          titulo="Tiempo De Espera Estimado"
          valor={loading ? '...' : `${fila?.tiempoEstimadoMinutos ?? 0} min`}
        />
      </section>

      <section className="seccion-formulario">
        <div className="contenedor-input">
          <input
            type="text"
            value={nombreCliente}
            onChange={(event) => setNombreCliente(event.target.value)}
            placeholder="NOMBRE"
          />

          <button type="button" onClick={() => setNombreCliente('')}>×</button>
        </div>

        <p>Ingrese su nombre para anotarse en la fila</p>

        {error && <p className="cliente-error">{error}</p>}

        <div onClick={handleSubmit}>
          <BotonPrincipal>
            {submitting ? 'Anotando...' : 'Anotarme a la fila'}
          </BotonPrincipal>
        </div>
      </section>

      <footer className="logo-dq">DQ</footer>
    </main>
  )
}

export default Inicio
