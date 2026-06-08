import api from './api'

const publicTurnoService = {
  getEstado: async (tokenPublico) => {
    const response = await api.get(`/public/turnos/${tokenPublico}`)
    return response.data
  },
  cancelar: async (tokenPublico) => {
    const response = await api.delete(`/public/turnos/${tokenPublico}`)
    return response.data
  },
}

export default publicTurnoService
