import api from './api'

const statsService = {
  getSummary: async (filaId) => {
    const response = await api.get('/admin/dashboard/stats', {
      params: filaId ? { filaId } : undefined,
    })
    return response.data
  },
  getRecentEvents: async (filaId) => {
    const response = await api.get('/admin/dashboard/recent-events', {
      params: filaId ? { filaId } : undefined,
    })
    const data = response.data
    return Array.isArray(data) ? data : []
  },
  getChartData: async (filaId) => {
    const response = await api.get('/admin/dashboard/activity-chart', {
      params: filaId ? { filaId } : undefined,
    })
    const data = response.data
    return Array.isArray(data) ? data : []
  },
}

export default statsService
