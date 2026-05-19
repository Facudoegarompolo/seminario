import api from './api'

const dashboardService = {
  getSummary: async () => {
    const response = await api.get('/admin/dashboard/summary')
    return response.data
  },
  getRealtimeEvents: async () => {
    const response = await api.get('/admin/dashboard/realtime')
    return response.data
  },
}

export default dashboardService
