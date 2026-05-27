import api from './api'

const authService = {
  login: async (credentials) => {
    const response = await api.post('/admin/auth/login', credentials)
    return response.data
  },
  getToken: () => localStorage.getItem('dq_admin_token'),
  setToken: (token) => localStorage.setItem('dq_admin_token', token),
  logout: () => {
    localStorage.removeItem('dq_admin_token')
  },
}

export default authService
