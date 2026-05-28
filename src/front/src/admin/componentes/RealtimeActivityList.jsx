function RealtimeActivityList({ events = [] }) {
  return (
    <div className="activity-list-card">
      {events.length > 0 ? (
        events.slice(0, 4).map((event) => (
          <div key={event.id} className="activity-row">
            <div className="activity-left">
              <span className="activity-number">#{event.number}</span>
              <span className="activity-label">{event.status}</span>
            </div>
            <span className="activity-time">{event.time}</span>
          </div>
        ))
      ) : (
        <div className="activity-empty">No hay eventos recientes</div>
      )}
    </div>
  )
}

export default RealtimeActivityList
