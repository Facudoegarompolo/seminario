import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

import logoMcDonalds from '../assets/mcdonalds.webp'
import TarjetaEstado from '../componentes/TarjetaEstado'

function Confirmacion() {
    const navigate = useNavigate()

    useEffect(() => {
        const timer = setTimeout(() => {
            navigate('/estado')
        }, 2500)

        return () => clearTimeout(timer)
    }, [navigate])

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

                <p>
                    Ya estás en la lista de espera.
                </p>
            </section>

            <section className="grilla-estado grilla-confirmacion">
                <TarjetaEstado
                    titulo="Hora en que debe presentarse"
                    valor="9:23 PM"
                />

                <TarjetaEstado
                    titulo="Tiempo de espera estimado"
                    valor="15 min"
                />

                <TarjetaEstado
                    titulo="Mi número"
                    valor="23"
                />

                <TarjetaEstado
                    titulo="Puesto en fila"
                    valor="5"
                />
            </section>

            <section className="mensaje-exito">
                <span>ⓘ</span>
                <p>Te avisaremos cuando sea casi tu turno.</p>
            </section>

            <footer className="logo-dq">
                DQ
            </footer>

        </main>
    )
}

export default Confirmacion