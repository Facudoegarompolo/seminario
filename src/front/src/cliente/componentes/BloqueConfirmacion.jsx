import TarjetaEstado from './TarjetaEstado'

function BloqueConfirmacion() {
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
                <TarjetaEstado titulo="Hora en que debe presentarse" valor="9:23 PM" />
                <TarjetaEstado titulo="Tiempo de espera estimado" valor="15 min" />
                <TarjetaEstado titulo="Mi número" valor="23" />
                <TarjetaEstado titulo="Puesto en fila" valor="5" />
            </section>

            <section className="mensaje-exito">
                <span>ⓘ</span>
                <p>Te avisaremos cuando sea casi tu turno.</p>
            </section>
        </>
    )
}

export default BloqueConfirmacion