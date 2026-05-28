import logoMcDonalds from '../assets/mcdonalds.webp'
import BarraProgreso from '../componentes/BarraProgreso'
import TarjetaEstado from '../componentes/TarjetaEstado'

function Estado() {
  return (
    <main className="pantalla">

      <section className="marca marca-estado">
        <img
          src={logoMcDonalds}
          alt="Logo McDonalds"
          className="logo-mcdonalds"
        />
      </section>

      <BarraProgreso porcentaje={45} />

      <section className="grilla-estado">
        <TarjetaEstado
          titulo="Hora En Que Debe Presentarse"
          valor="9:23 PM"
        />

        <TarjetaEstado
          titulo="Tiempo De Espera"
          valor="4 minutos"
        />

        <TarjetaEstado
          titulo="Mi Número"
          valor="23"
        />

        <TarjetaEstado
          titulo="Puesto En Fila"
          valor="5"
        />
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

      <footer className="logo-dq">
        DQ
      </footer>

    </main>
  )
}

export default Estado