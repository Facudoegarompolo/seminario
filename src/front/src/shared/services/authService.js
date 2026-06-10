import api from './api'

const authService = {
  login: async (credentials) => {
    try {
      const response = await api.post('/admin/auth/login', credentials)
      const data = response.data

      if (!data?.token) {
        throw new Error('La respuesta de login no incluyó un token JWT.')
      }

      authService.setSession(data)
      return data
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'No se pudo iniciar sesión.'
      throw new Error(message, { cause: error })
    }
  },
  getToken: () => localStorage.getItem('dq_admin_token'),
  setToken: (token) => localStorage.setItem('dq_admin_token', token),
  setSession: (session) => {
    localStorage.setItem('dq_admin_token', session.token)
    localStorage.setItem(
      'dq_admin_user',
      JSON.stringify({
        usuarioId: session.usuarioId,
        localId: session.localId,
        nombre: session.nombre,
        email: session.email,
        rol: session.rol,
      }),
    )
  },
  getUser: () => {
    const rawUser = localStorage.getItem('dq_admin_user')
    if (!rawUser) return null

    try {
      return JSON.parse(rawUser)
    } catch {
      return null
    }
  },
  logout: () => {
    localStorage.removeItem('dq_admin_token')
    localStorage.removeItem('dq_admin_user')
    localStorage.removeItem('dq_admin_remember')
  },
}

export default authService
