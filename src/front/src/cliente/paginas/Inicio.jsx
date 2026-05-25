import TarjetaInfo from '../componentes/TarjetaInfo'
import BotonPrincipal from '../componentes/BotonPrincipal'
import logoMcDonalds from '../assets/mcdonalds.webp'
import { useNavigate } from 'react-router-dom'
//import { useState } from 'react'
import SelectorCantidad from '../componentes/SelectorCantidad'
import { useState, useEffect } from 'react'

function Inicio() {
    const navigate = useNavigate()

    const [nombreCliente, setNombreCliente] = useState('')
    const [cantidadIntegrantes, setCantidadIntegrantes] = useState(1)
    const [estadoFila, setEstadoFila] = useState(null)
    const crearTurno = async () => {

        try {

            const response = await fetch(
                'http://192.168.0.103:8080/api/public/filas/starbucks-uade/turnos',
                {

                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },

                    body: JSON.stringify({
                        nombreCliente,
                        cantidadIntegrantes
                    })
                }
            )

            const data = await response.json()

            console.log(data)

            navigate('/estado', { state: data })//integramos json 
        } catch (error) {

            console.error(error)

        }
    }
    useEffect(() => {

        const obtenerEstadoFila = async () => {

            try {

                const response = await fetch(
                    'http://192.168.0.103:8080/api/public/filas/starbucks-uade/estado',)

                const data = await response.json()

                console.log(data)

                setEstadoFila(data)

            } catch (error) {

                console.error(error)

            }
        }

        obtenerEstadoFila()

    }, [])
    return (

        <main className="pantalla">

            <section className="marca">
                <img
                    src={logoMcDonalds}
                    alt="Logo McDonalds"
                    className="logo-mcdonalds" />
            </section>

            <section className="seccion-info">

                <TarjetaInfo
                    titulo="Gente En Fila"
                    valor={estadoFila?.personasEsperando ?? 0}
                />

                <TarjetaInfo
                    titulo="Tiempo De Espera Estimado"
                    valor={`${estadoFila?.tiempoEstimadoMinutos ?? 0} min`}
                />

            </section>

            <section className="seccion-formulario">

                <SelectorCantidad
                    cantidad={cantidadIntegrantes}
                    setCantidad={setCantidadIntegrantes} />

                <div className="contenedor-input">

                    <input
                        type="text"
                        placeholder="NOMBRE"
                        value={nombreCliente}
                        onChange={(e) => setNombreCliente(e.target.value)}
                    />

                    <span>×</span>

                </div>

                <p>
                    Ingrese su nombre para anotarse en la fila
                </p>

                <div

                    onClick={crearTurno}                  >
                    <BotonPrincipal>
                        Anotarme
                    </BotonPrincipal>
                </div>

            </section>

            <footer className="logo-dq">
                DQ
            </footer>

        </main>
    )
}

export default Inicio