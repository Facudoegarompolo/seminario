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
        descripcion: 'Faltan 3 personas antes que vos.',
        tiempo: '5 min',
        puesto: '4',
        mensaje: 'Por favor, acércate al mostrador cuando sea tu turno.'
    },

    aviso2: {
        icono: <Bell size={82} strokeWidth={1.8} />,
        clase: 'aviso naranja',
        titulo: '¡Estás próximo!',
        descripcion: 'Falta 1 persona antes que vos.',
        tiempo: '2 min',
        puesto: '2',
        mensaje: 'Prepárate, pronto será tu turno.'
    },

    turno: {
        icono: <Bell size={82} strokeWidth={1.8} />,
        clase: 'aviso negro',
        titulo: '¡Es tu turno!',
        descripcion: 'Presentate en el mostrador para ser atendido.',
        tiempo: '0 min',
        puesto: '1',
        mensaje: 'Tenés 2 minutos para presentarte.'
    },

    tardanza: {
        icono: <CircleAlert size={82} strokeWidth={1.8} />,
        clase: 'aviso rojo',
        titulo: '¡Aún estás a tiempo!',
        descripcion: 'Llegaste tarde, pero aún tenés unos minutos.',
        tiempo: '00:45',
        puesto: 'En espera',
        mensaje: 'Si no te presentás a tiempo, perderás tu lugar.'
    },

    perdido: {
        icono: <CircleX size={92} strokeWidth={1.8} />,
        clase: 'aviso perdido',
        titulo: 'Perdiste tu lugar',
        descripcion: 'No te presentaste a tiempo.',
        tiempo: '--',
        puesto: '--',
        mensaje: 'Podés volver a anotarte en la fila cuando quieras.'
    },

    finalizado: {
        icono: <CircleCheckBig size={92} strokeWidth={1.8} />,
        clase: 'aviso finalizado',
        titulo: 'Gracias por su tiempo',
        descripcion: 'Tu atención fue atendida con éxito.',
        tiempo: '',
        puesto: '',
        mensaje: 'Te esperamos pronto en McDonald’s.'
    }

}
function AvisoTurno({ tipo }) {
    const aviso = avisos[tipo]

    return (
        <>
            <section className="bloque-aviso">
                <div className={`icono-aviso ${tipo}`}>
                    {aviso.icono}
                </div>

                <h1>{aviso.titulo}</h1>
                <p>{aviso.descripcion}</p>
            </section>

            <section className="grilla-estado">
                <TarjetaEstado titulo="Hora en que debe presentarse" valor="9:23 PM" />
                <TarjetaEstado titulo="Tiempo de espera estimado" valor={aviso.tiempo} />
                <TarjetaEstado titulo="Mi número" valor="23" />
                <TarjetaEstado titulo="Puesto en fila" valor={aviso.puesto} />
            </section>

            <section className={`mensaje-aviso ${tipo}`}>
                <p>ⓘ{aviso.mensaje}</p>
            </section>
        </>
    )
}

export default AvisoTurno