import BarraProgreso from './BarraProgreso'
import TarjetaEstado from './TarjetaEstado'

function BloqueSeguimiento() {
    return (
        <>
            <BarraProgreso porcentaje={55} />

            <section className="grilla-estado">
                <TarjetaEstado titulo="Hora en que debe presentarse" valor="9:23 PM" />
                <TarjetaEstado titulo="Tiempo de espera" valor="4 minutos" />
                <TarjetaEstado titulo="Mi número" valor="23" />
                <TarjetaEstado titulo="Puesto en fila" valor="5" />
            </section>

            <section className="notificacion">
                <div>
                    <h3>Notificación de turno</h3>
                    <p>Active si quiere que le avisemos su turno</p>
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