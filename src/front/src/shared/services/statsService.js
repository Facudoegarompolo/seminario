import api from './api'

const statsService = {
  getSummary: async () => {
    const response = await api.get('/admin/dashboard/stats')
    return response.data
  },

  getRecentEvents: async () => {
    const response = await api.get(
      '/admin/dashboard/recent-events'
    )

    return response.data
  },

  getChartData: async () => {
    const response = await api.get(
      '/admin/dashboard/activity-chart'
    )

    return response.data
  },
}

export default statsService