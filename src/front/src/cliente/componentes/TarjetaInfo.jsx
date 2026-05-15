function TarjetaInfo({ titulo, valor }) {
    return (
        <div className="tarjeta-info">
            <p>{titulo}</p>
            <h2>{valor}</h2>
        </div>
    )
}

export default TarjetaInfo