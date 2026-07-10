import api from './api'

const queueService = {
  getTurnos: async () => {
    const response = await api.get('/admin/fila/turnos', {
      params: { _t: Date.now() },
      headers: { 'Cache-Control': 'no-cache' },
    })
    const data = response.data
    return Array.isArray(data) ? data : []
  },
  llamarSiguiente: async () => {
    const response = await api.post('/admin/fila/llamar-siguiente')
    return response.data
  },
  finalizarTurno: async (turnoId) => {
    const response = await api.post(`/admin/turnos/${turnoId}/finalizar`)
    return response.data
  },
  marcarNoPresentado: async (turnoId) => {
    const response = await api.post(`/admin/turnos/${turnoId}/no-presentado`)
    return response.data
  },
  quitarAtendidos: async () => {
    const response = await api.post('/admin/fila/limpiar-atendidos')
    return response.data
  },
  quitarDeFilaVirtual: async (turnoId) => {
    await api.delete(`/admin/turnos/${turnoId}/fila-virtual`)
  },
}

export default queueService
