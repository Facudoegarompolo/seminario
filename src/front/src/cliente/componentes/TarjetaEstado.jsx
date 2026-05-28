function TarjetaEstado({ titulo, valor }) {
    return (
        <article className="tarjeta-estado">
            <p>{titulo}</p>
            <h2>{valor}</h2>
        </article>
    )
}

export default TarjetaEstado