import api from './api'

const DEFAULT_FILA_ID = 1

const queueService = {
  getTurnos: async (filaId = DEFAULT_FILA_ID) => {
    const response = await api.get(`/filas/${filaId}/turnos`)
    const data = response.data

    return Array.isArray(data) ? data : []
  },

  llamarSiguiente: async (filaId = DEFAULT_FILA_ID) => {
    const response = await api.post(
      `/filas/${filaId}/llamar-siguiente`
    )

    return response.data
  },

  finalizarTurno: async (turnoId) => {
    const response = await api.post(
      `/turnos/${turnoId}/finalizar`
    )

    return response.data
  },

  marcarNoPresentado: async (turnoId) => {
    const response = await api.post(
      `/turnos/${turnoId}/no-presentado`
    )

    return response.data
  },
}

export default queueService