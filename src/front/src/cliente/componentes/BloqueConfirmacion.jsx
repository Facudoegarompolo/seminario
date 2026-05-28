import TarjetaEstado from './TarjetaEstado'

function BloqueConfirmacion({ turno }) {

    const horaActual = new Date()

    horaActual.setMinutes(
        horaActual.getMinutes() + (turno?.tiempoEstimadoMinutos || 0)
    )

    const horaPresentacion = horaActual.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    })

    return (
        <>
            <section className="confirmacion">
                <div className="circulo-check">
                    ✓
                </div>

                <h1>¡Te uniste a la fila!</h1>

                <p>Ya estás en la lista de espera.</p>
            </section>

            <section className="grilla-estado">

                <TarjetaEstado
                    titulo="Hora en que debe presentarse"
                    valor={horaPresentacion}
                />

                <TarjetaEstado
                    titulo="Tiempo de espera estimado"
                    valor={`${turno?.tiempoEstimadoMinutos} min`}
                />

                <TarjetaEstado
                    titulo="Mi número"
                    valor={turno?.numeroTurno}
                />

                <TarjetaEstado
                    titulo="Puesto en fila"
                    valor={turno?.personasAdelante + 1}
                />

            </section>

            <section className="mensaje-exito">
                <span>ⓘ</span>

                <p>
                    Te avisaremos cuando sea casi tu turno.
                </p>
            </section>
        </>
    )
}

export default BloqueConfirmacion