import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'

import logoMcDonalds from '../assets/mcdonalds.webp'
import BloqueConfirmacion from '../componentes/BloqueConfirmacion'
import BloqueSeguimiento from '../componentes/BloqueSeguimiento'
import AvisoTurno from '../componentes/AvisoTurno'

function Estado() {
  const location = useLocation()

  const [turnoActual, setTurnoActual] = useState(location.state)
  const [fase, setFase] = useState('confirmacion')

  /*
   * Modo demo:
   * Simula el avance de la fila desde el frontend.
   * Cada 10 segundos baja una persona adelante.
   * En el sistema real, este avance debería venir del backend/admin
   * cuando el local llama o finaliza turnos.
   */
  const [personasDemo, setPersonasDemo] = useState(
    location.state?.personasAdelante ?? 0
  )

  function mapearTurnoAFase(turno) {
    if (!turno) return 'confirmacion'

    if (turno.estado === 'LLAMADO') return 'turno'
    if (turno.estado === 'FINALIZADO') return 'finalizado'
    if (turno.estado === 'CANCELADO') return 'perdido'
    if (turno.estado === 'NO_PRESENTADO') return 'perdido'

    if (turno.personasAdelante <= 1) return 'aviso2'
    if (turno.personasAdelante <= 3) return 'aviso1'

    return 'seguimiento'
  }

  const turnoVisual = {
    ...turnoActual,
    personasAdelante: personasDemo,
    tiempoEstimadoMinutos: personasDemo * 3,
    estado: personasDemo === 0 ? 'LLAMADO' : turnoActual?.estado
  }
  const salirDeFila = async () => {
    try {

      const response = await fetch(

        `${import.meta.env.VITE_API_URL}/api/public/turnos/${turnoActual.tokenPublico}`,
        {
          method: 'DELETE'
        }
      )

      const data = await response.json()

      setTurnoActual(data)
      setFase('perdido')

    } catch (error) {

      console.error(error)

    }
  }
  useEffect(() => {
    const timer = setTimeout(() => {
      setFase(mapearTurnoAFase(turnoVisual))
    }, 2500)

    return () => clearTimeout(timer)
  }, [])

  useEffect(() => {
    if (!turnoActual?.tokenPublico) return

    const consultarTurno = async () => {
      try {
        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/api/public/turnos/${turnoActual.tokenPublico}`,)

        const data = await response.json()

        setTurnoActual(data)

        /*
         * Sincroniza la simulación visual con el backend.
         * Si el backend devuelve menos personas adelante, usamos ese valor.
         * Si devuelve más, mantenemos el avance visual para no retroceder en la demo.
         */
        setPersonasDemo((personasActualesDemo) =>
          Math.min(personasActualesDemo, data.personasAdelante ?? personasActualesDemo)
        )

      } catch (error) {
        console.error(error)
      }
    }

    const intervalo = setInterval(consultarTurno, 5000)

    return () => clearInterval(intervalo)
  }, [turnoActual?.tokenPublico])

  useEffect(() => {
    const intervaloDemo = setInterval(() => {
      setPersonasDemo((personasActuales) => {
        if (personasActuales <= 0) {
          return 0
        }

        return personasActuales - 1
      })
    }, 10000)

    return () => clearInterval(intervaloDemo)
  }, [])

  useEffect(() => {
    setFase(mapearTurnoAFase(turnoVisual))
  }, [personasDemo, turnoActual?.estado])
  useEffect(() => {
    if (fase !== 'turno') return

    const timer = setTimeout(() => {
      setFase('perdido') //cambiamos por perdido o finalizado 
    }, 10000)

    return () => clearTimeout(timer)
  }, [fase])
  return (
    <main className="pantalla">

      <section className="marca marca-estado">
        <img
          src={logoMcDonalds}
          alt="Logo McDonalds"
          className="logo-mcdonalds"
        />
      </section>

      {
        fase === 'confirmacion' ? (
          <BloqueConfirmacion turno={turnoVisual} />
        ) : fase === 'seguimiento' ? (
          <BloqueSeguimiento turno={turnoVisual} />
        ) : fase === 'aviso1' ? (
          <AvisoTurno tipo="aviso1" turno={turnoVisual} />
        ) : fase === 'aviso2' ? (
          <AvisoTurno tipo="aviso2" turno={turnoVisual} />
        ) : fase === 'turno' ? (
          <AvisoTurno tipo="turno" turno={turnoVisual} />
        ) : fase === 'tardanza' ? (
          <AvisoTurno tipo="tardanza" turno={turnoVisual} />
        ) : fase === 'perdido' ? (
          <AvisoTurno tipo="perdido" turno={turnoVisual} />
        ) : fase === 'finalizado' ? (
          <AvisoTurno tipo="finalizado" turno={turnoVisual} />
        ) : null
      }
      {
        fase !== 'perdido' &&
        fase !== 'finalizado' && (
          <button
            className="boton-salir-fila"
            onClick={salirDeFila}
          >
            Salir de la fila
          </button>
        )
      }
      <footer className="logo-dq">
        DQ
      </footer>

    </main>
  )
}

export default Estado