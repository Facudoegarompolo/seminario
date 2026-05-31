import { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'

import logoMcDonalds from '../assets/mcdonalds.webp'
import TarjetaEstado from '../componentes/TarjetaEstado'

function Confirmacion() {
    const navigate = useNavigate()
    const location = useLocation()

    // Recibe los datos del turno que manda Inicio.jsx
    const turno = location.state

    // Calcula la hora estimada de presentación
    const horaEstimada = new Date()
    horaEstimada.setMinutes(
        horaEstimada.getMinutes() + (turno?.tiempoEstimadoMinutos || 0)
    )
    const horaPresentacion = horaEstimada.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    })

    useEffect(() => {


        const timer = setTimeout(() => {
            // Le pasa los datos del turno a la pantalla de estado
            navigate('/estado', { state: turno })
        }, 2500)

        return () => clearTimeout(timer)
    }, [navigate, turno])

    return (
        <main className="pantalla">

            <section className="marca">
                <img
                    src={logoMcDonalds}
                    alt="Logo McDonalds"
                    className="logo-mcdonalds"
                />
                <p>1968</p>
            </section>

            <section className="confirmacion">
                <div className="circulo-check">
                    ✓
                </div>

                <h1>¡Te uniste a la fila!</h1>

                <p>Ya estás en la lista de espera.</p>
            </section>

            <section className="grilla-estado grilla-confirmacion">

                <TarjetaEstado
                    titulo="Hora en que debe presentarse"
                    valor={horaPresentacion}              // antes: "9:23 PM"
                />

                <TarjetaEstado
                    titulo="Tiempo de espera estimado"
                    valor={`${turno?.tiempoEstimadoMinutos ?? 0} min`}  // antes: "15 min"
                />

                <TarjetaEstado
                    titulo="Mi número"
                    valor={turno?.numeroTurno ?? '-'}     // antes: "23"
                />

                <TarjetaEstado
                    titulo="Puesto en fila"
                    valor={(turno?.personasAdelante ?? 0) + 1}  // antes: "5"
                />

            </section>

            <section className="mensaje-exito">
                <span>ⓘ</span>
                <p>Te avisaremos cuando sea casi tu turno.</p>
            </section>

            <footer className="logo-dq">
                DQ
            </footer>

        </main >
    )
}

export default Confirmacion