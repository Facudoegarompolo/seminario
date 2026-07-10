import { useEffect, useState } from 'react'
import statsService from '../../shared/services/statsService'
import BackButton from '../componentes/BackButton'
import '../estilos/Estadisticas.css'

const dateLabelFormatter = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit',
  month: 'short',
})

const historyDateFormatter = new Intl.DateTimeFormat('es-AR', {
  weekday: 'short',
  day: '2-digit',
  month: 'short',
})

const historyTimeFormatter = new Intl.DateTimeFormat('es-AR', {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

const buildFilterOptions = () => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)

  return [
    `Hoy, ${dateLabelFormatter.format(today)}`,
    `Ayer, ${dateLabelFormatter.format(yesterday)}`,
    'Semana',
  ]
}

const FILTER_OPTIONS = buildFilterOptions()

const DEFAULT_SUMMARY = {
  atendidosHoy: 0,
  tiempoPromedio: '0 min',
  ausentes: 0,
  maximoEspera: '0 min',
}

const parseEventDate = (value) => {
  if (!value) {
    return null
  }

  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const getHistoryTimestamp = (event) => {
  const parsedDate = parseEventDate(event.dateTime)

  if (!parsedDate) {
    return {
      date: event.date || '',
      time: event.time || '',
    }
  }

  return {
    date: historyDateFormatter.format(parsedDate),
    time: historyTimeFormatter.format(parsedDate),
  }
}

function Estadisticas() {
  const [summary, setSummary] = useState(DEFAULT_SUMMARY)
  const [events, setEvents] = useState([])
  const [chartData, setChartData] = useState([])
  const [selectedFilter, setSelectedFilter] = useState(FILTER_OPTIONS[0])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true)

      try {
        const [summaryData, eventData, chartDataResponse] = await Promise.all([
          statsService.getSummary(),
          statsService.getRecentEvents(),
          statsService.getChartData(),
        ])

        setSummary(summaryData || DEFAULT_SUMMARY)
        setEvents(Array.isArray(eventData) ? eventData : [])
        setChartData(Array.isArray(chartDataResponse) ? chartDataResponse : [])
      } catch {
        setSummary(DEFAULT_SUMMARY)
        setEvents([])
        setChartData([])
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [selectedFilter])

  return (
    <div className="estadisticas-page">
      <BackButton to="/admin/dashboard" />
      <header className="estadisticas-header">
        <div>
          <h2>Estadisticas</h2>
          <p>Monitorea metricas e historial del sistema</p>
        </div>
        <select value={selectedFilter} onChange={(event) => setSelectedFilter(event.target.value)}>
          {FILTER_OPTIONS.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </header>

      <section className="estadisticas-summary-grid">
        <div className="estadistica-card">
          <span>Atendidos</span>
          <strong>{loading ? '...' : summary.atendidosHoy}</strong>
        </div>
        <div className="estadistica-card">
          <span>No se presentaron</span>
          <strong>{loading ? '...' : summary.ausentes}</strong>
        </div>
        <div className="estadistica-card">
          <span>Tiempo promedio de espera</span>
          <strong>{loading ? '...' : summary.tiempoPromedio}</strong>
        </div>
        <div className="estadistica-card">
          <span>Maximo de espera</span>
          <strong>{loading ? '...' : summary.maximoEspera}</strong>
        </div>
      </section>

      <section className="estadisticas-chart-card">
        <div className="chart-header">
          <h3>Grafico de actividad</h3>
        </div>

        <div className="chart-plot">
          {loading ? (
            <p className="chart-empty">Cargando datos...</p>
          ) : (() => {
            const displayData = chartData

            if (displayData.length === 0) {
              return (
                <p className="chart-empty">
                  No hay datos reales para mostrar
                </p>
              )
            }

            const CHART_WIDTH = 400
            const CHART_HEIGHT = 220

            const PADDING_LEFT = 10
            const PADDING_RIGHT = 10
            const PADDING_TOP = 10
            const PADDING_BOTTOM = 30

            const usableWidth =
              CHART_WIDTH - PADDING_LEFT - PADDING_RIGHT

            const usableHeight =
              CHART_HEIGHT - PADDING_TOP - PADDING_BOTTOM

            const maxValue = Math.max(
              ...displayData.map((entry) => entry.value),
              1
            )

            const axisValues = [
              maxValue,
              Math.ceil(maxValue / 2),
              0,
            ]

            const points = displayData.map((item, index) => {
              const x =
                displayData.length === 1
                  ? CHART_WIDTH / 2
                  : PADDING_LEFT +
                  (index / (displayData.length - 1)) *
                  usableWidth

              const y =
                CHART_HEIGHT -
                PADDING_BOTTOM -
                (item.value / maxValue) * usableHeight

              return {
                ...item,
                x,
                y,
              }
            })

            return (
              <div className="chart-visualization">
                <div className="chart-axis">
                  {axisValues.map((value) => (
                    <span key={value}>{value}</span>
                  ))}
                </div>

                <div className="chart-graph">
                  <svg
                    className="chart-svg"
                    viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
                  >
                    {axisValues.map((_, index) => {
                      const y =
                        PADDING_TOP +
                        (index / (axisValues.length - 1)) *
                        usableHeight

                      return (
                        <line
                          key={index}
                          x1={PADDING_LEFT}
                          y1={y}
                          x2={CHART_WIDTH - PADDING_RIGHT}
                          y2={y}
                          className="chart-grid-line"
                        />
                      )
                    })}

                    <line
                      x1={PADDING_LEFT}
                      y1={CHART_HEIGHT - PADDING_BOTTOM}
                      x2={CHART_WIDTH - PADDING_RIGHT}
                      y2={CHART_HEIGHT - PADDING_BOTTOM}
                      className="chart-x-axis"
                    />

                    <polyline
                      className="chart-line"
                      fill="none"
                      points={points
                        .map((item) => `${item.x},${item.y}`)
                        .join(' ')}
                    />

                    {points.map((item) => (
                      <circle
                        key={item.label}
                        className="chart-point"
                        cx={item.x}
                        cy={item.y}
                        r="4"
                      />
                    ))}
                  </svg>

                  <div
                    className="chart-labels"
                    style={{
                      gridTemplateColumns: `repeat(${displayData.length}, minmax(0, 1fr))`,
                    }}
                  >
                    {displayData.map((item) => (
                      <span key={item.label}>
                        {item.label}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )
          })()}
        </div>
      </section>

      <section className="estadisticas-history-card">
        <h3>Historial reciente</h3>
        <div className="history-list">
          {events.length > 0 ? (
            events.map((event) => {
              const timestamp = getHistoryTimestamp(event)

              return (
                <div key={event.id} className="history-row">
                  <time className="history-time" dateTime={event.dateTime || undefined}>
                    <span>{timestamp.time || '--:--'}</span>
                    {timestamp.date ? <small>{timestamp.date}</small> : null}
                  </time>
                  <p>{event.text}</p>
                </div>
              )
            })
          ) : (
            <p className="history-empty">
              {loading ? 'Cargando historial...' : 'No hay eventos recientes'}
            </p>
          )}
        </div>
      </section>
    </div>
  )
}

export default Estadisticas
