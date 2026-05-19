import api from './api'

const mockSummary = {
  atendidosHoy: 128,
  tiempoPromedio: '18 min',
  ausentes: 12,
  maximoEspera: '45 min',
}

const mockRecentEvents = [
  { id: '1', time: '9:28 PM', text: 'Se llamó al #23' },
  { id: '2', time: '9:27 PM', text: 'Se llamó al #22' },
  { id: '3', time: '9:25 PM', text: 'Se atendió al #21' },
]

const mockChartData = [
  { label: '12 PM', value: 12 },
  { label: '3 PM', value: 18 },
  { label: '6 PM', value: 32 },
  { label: '9 PM', value: 24 },
]

const statsService = {
  getSummary: async () => {
    try {
      const response = await api.get('/admin/dashboard/stats')
      return response.data
    } catch (error) {
      return mockSummary
    }
  },
  getRecentEvents: async () => {
    try {
      const response = await api.get('/admin/dashboard/recent-events')
      const data = response.data
      return Array.isArray(data) ? data : mockRecentEvents
    } catch (error) {
      return mockRecentEvents
    }
  },
  getChartData: async () => {
    try {
      const response = await api.get('/admin/dashboard/activity-chart')
      const data = response.data
      return Array.isArray(data) ? data : mockChartData
    } catch (error) {
      return mockChartData
    }
  },
}

export default statsService
