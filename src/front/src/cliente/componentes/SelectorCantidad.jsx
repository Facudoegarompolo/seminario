import { useState } from 'react'

function SelectorCantidad() {

    const [cantidad, setCantidad] = useState(2)

    function aumentar() {
        setCantidad(cantidad + 1)
    }

    function disminuir() {

        if (cantidad > 1) {
            setCantidad(cantidad - 1)
        }

    }

    return (

        <section className="selector-cantidad">

            <p>CANTIDAD DE CLIENTES</p>

            <div className="contador">

                <button onClick={disminuir}>
                    -
                </button>

                <span>{cantidad}</span>

                <button onClick={aumentar}>
                    +
                </button>

            </div>

            <small>
                Incluyéndote a vos
            </small>

        </section>

    )

}

export default SelectorCantidad