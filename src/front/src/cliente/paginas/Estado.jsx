import { useEffect, useState } from 'react'

import logoMcDonalds from '../assets/mcdonalds.webp'
import BloqueConfirmacion from '../componentes/BloqueConfirmacion'
import BloqueSeguimiento from '../componentes/BloqueSeguimiento'
import AvisoTurno from '../componentes/AvisoTurno'

function Estado() {
  const [fase, setFase] = useState('aviso2')

  useEffect(() => {
    if (fase !== 'confirmacion') {
      return
    }

    const timer = setTimeout(() => {
      setFase('seguimiento')
    }, 2500)

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
          <BloqueConfirmacion />
        ) : fase === 'seguimiento' ? (
          <BloqueSeguimiento />
        ) : fase === 'aviso1' ? (
          <AvisoTurno tipo="aviso1" />
        ) : fase === 'aviso2' ? (
          <AvisoTurno tipo="aviso2" />
        ) : fase === 'turno' ? (
          <AvisoTurno tipo="turno" />
        ) : fase === 'tardanza' ? (
          <AvisoTurno tipo="tardanza" />
        ) : fase === 'perdido' ? (
          <AvisoTurno tipo="perdido" />
        ) : fase === 'finalizado' ? (
          <AvisoTurno tipo="finalizado" />
        ) : null
      }

      <footer className="logo-dq">
        DQ
      </footer>

    </main>
  )
}

export default Estado