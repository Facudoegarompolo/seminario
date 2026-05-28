import BarraProgreso from './BarraProgreso'
import TarjetaEstado from './TarjetaEstado'

function BloqueSeguimiento({ turno }) {

    const horaActual = new Date()

    horaActual.setMinutes(
        horaActual.getMinutes() + (turno?.tiempoEstimadoMinutos || 0)
    )

    const horaPresentacion = horaActual.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    })

    const porcentaje = Math.max(
        5,
        100 - ((turno?.personasAdelante || 0) * 20)
    )

    return (
        <>
            <BarraProgreso porcentaje={porcentaje} />

            <section className="grilla-estado">

                <TarjetaEstado
                    titulo="Hora en que debe presentarse"
                    valor={horaPresentacion}
                />

                <TarjetaEstado
                    titulo="Tiempo de espera"
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

            <section className="notificacion">
                <div>
                    <h3>Notificación de turno</h3>

                    <p>
                        Active si quiere que le avisemos su turno
                    </p>
                </div>

                <label className="switch">
                    <input type="checkbox" />
                    <span></span>
                </label>
            </section>
        </>
    )
}

export default BloqueSeguimiento