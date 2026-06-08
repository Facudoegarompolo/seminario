import api from './api'

const dashboardService = {
  getSummary: async (filaId) => {
    const response = await api.get('/admin/dashboard/summary', {
      params: filaId ? { filaId } : undefined,
    })
    return response.data
  },
  getRealtimeEvents: async (filaId) => {
    const response = await api.get('/admin/dashboard/realtime', {
      params: filaId ? { filaId } : undefined,
    })
    return response.data
  },
}

export default dashboardService
