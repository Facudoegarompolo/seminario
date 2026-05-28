function TurnoCard({ turno, onCall, onFinish, onCancel, onDetail }) {
  return (
    <article className="turno-card">
      <div className="turno-card-main">
        <div className="turno-number">#{turno.numeroTurno}</div>
        <div>
          <p className="turno-state">{turno.estadoLabel}</p>
          <p className="turno-meta">
            {turno.personas} persona{turno.personas !== 1 ? 's' : ''} · {turno.tiempoEstimado}
          </p>
        </div>
      </div>

      <div className="turno-actions">
        <button type="button" className="turno-action small" onClick={() => onCall(turno)}>
          Llamar
        </button>
        <button type="button" className="turno-action small" onClick={() => onFinish(turno)}>
          Atendido
        </button>
        <button type="button" className="turno-action small outline" onClick={() => onCancel(turno)}>
          Cancelar
        </button>
        <button type="button" className="turno-action small outline" onClick={() => onDetail(turno)}>
          Detalle
        </button>
      </div>
    </article>
  )
}

export default TurnoCard
