import { useEffect, useState } from 'react'
import persona from '../assets/personita.webp'

function BarraProgreso({ porcentaje }) {
    const [progreso, setProgreso] = useState(0)

    useEffect(() => {
        const timer = setTimeout(() => {
            setProgreso(porcentaje)
        }, 300)

        return () => clearTimeout(timer)
    }, [porcentaje])

    return (
        <div className="contenedor-barra">
            <div
                className="etiqueta-usuario"
                style={{ left: `${progreso}%` }}
            >
                Usted esta aquí
            </div>

            <div className="barra-progreso">
                <div
                    className="barra-progreso-relleno"
                    style={{ width: `${progreso}%` }}
                />

                <img
                    src={persona}
                    alt="personita"
                    className="personita"
                    style={{ left: `${progreso}%` }}
                />
            </div>
        </div>
    )
}

export default BarraProgreso