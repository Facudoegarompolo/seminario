import TarjetaInfo from '../componentes/TarjetaInfo'
import BotonPrincipal from '../componentes/BotonPrincipal'
import logoMcDonalds from '../assets/mcdonalds.webp'
import { useNavigate } from 'react-router-dom'

function Inicio() {
    const navigate = useNavigate()
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
                    valor="4"
                />

                <TarjetaInfo
                    titulo="Tiempo De Espera Estimado"
                    valor="15 min"
                />

            </section>

            <section className="seccion-formulario">

                <div className="contenedor-input">

                    <input
                        type="text"
                        placeholder="NOMBRE"
                    />

                    <span>×</span>

                </div>

                <p>
                    Ingrese su nombre para anotarse en la fila
                </p>

                <div
                    onClick={() => navigate('/estado')}
                >
                    <BotonPrincipal>
                        Anotarme a la fila
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