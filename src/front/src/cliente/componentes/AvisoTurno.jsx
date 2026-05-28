import TarjetaEstado from './TarjetaEstado'
import {
    Bell,
    CircleAlert,
    CircleX,
    CircleCheckBig
} from 'lucide-react'

const avisos = {

    aviso1: {
        icono: <Bell size={82} strokeWidth={1.8} />,
        clase: 'aviso amarillo',
        titulo: 'Tu turno se acerca',
        mensaje: 'Por favor, acércate al mostrador cuando sea tu turno.'
    },

    aviso2: {
        icono: <Bell size={82} strokeWidth={1.8} />,
        clase: 'aviso naranja',
        titulo: '¡Estás próximo!',
        mensaje: 'Prepárate, pronto será tu turno.'
    },

    turno: {
        icono: <Bell size={82} strokeWidth={1.8} />,
        clase: 'aviso negro',
        titulo: '¡Es tu turno!',
        mensaje: 'Tenés 2 minutos para presentarte.'
    },

    tardanza: {
        icono: <CircleAlert size={82} strokeWidth={1.8} />,
        clase: 'aviso rojo',
        titulo: '¡Aún estás a tiempo!',
        mensaje: 'Si no te presentás a tiempo, perderás tu lugar.'
    },

    perdido: {
        icono: <CircleX size={92} strokeWidth={1.8} />,
        clase: 'aviso perdido',
        titulo: 'Perdiste tu lugar',
        mensaje: 'Podés volver a anotarte en la fila cuando quieras.'
    },

    finalizado: {
        icono: <CircleCheckBig size={92} strokeWidth={1.8} />,
        clase: 'aviso finalizado',
        titulo: 'Gracias por su tiempo',
        mensaje: 'Te esperamos pronto en McDonald’s.'
    }

}

function AvisoTurno({ tipo, turno }) {

    const aviso = avisos[tipo]

    const horaActual = new Date()

    horaActual.setMinutes(
        horaActual.getMinutes() + (turno?.tiempoEstimadoMinutos || 0)
    )

    const horaPresentacion = horaActual.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    })

    let descripcion = ''

    if (tipo === 'aviso1') {
        descripcion = `Faltan ${turno?.personasAdelante || 0} personas antes que vos.`
    }

    else if (tipo === 'aviso2') {
        descripcion = 'Falta 1 persona antes que vos.'
    }

    else if (tipo === 'turno') {
        descripcion = 'Presentate en el mostrador para ser atendido.'
    }

    else if (tipo === 'tardanza') {
        descripcion = 'Llegaste tarde, pero aún tenés unos minutos.'
    }

    else if (tipo === 'perdido') {
        descripcion = 'No te presentaste a tiempo.'
    }

    else if (tipo === 'finalizado') {
        descripcion = 'Tu atención fue realizada con éxito.'
    }

    return (
        <>
            <section className="bloque-aviso">

                <div className={`icono-aviso ${tipo}`}>
                    {aviso.icono}
                </div>

                <h1>{aviso.titulo}</h1>

                <p>{descripcion}</p>

            </section>

            <section className="grilla-estado">

                <TarjetaEstado
                    titulo="Hora en que debe presentarse"
                    valor={horaPresentacion}
                />

                <TarjetaEstado
                    titulo="Tiempo de espera estimado"
                    valor={`${turno?.tiempoEstimadoMinutos ?? 0} min`}
                />

                <TarjetaEstado
                    titulo="Mi número"
                    valor={turno?.numeroTurno}
                />

                <TarjetaEstado
                    titulo="Puesto en fila"
                    valor={(turno?.personasAdelante ?? 0) + 1}
                />

            </section>

            <section className={`mensaje-aviso ${tipo}`}>
                <p>
                    ⓘ {aviso.mensaje}
                </p>
            </section>
        </>
    )
}

export default AvisoTurno
