import api from './api'

const publicFilaService = {
  getEstado: async (codigoPublico) => {
    const response = await api.get(`/public/filas/${codigoPublico}/estado`)
    return response.data
  },
  crearTurno: async (codigoPublico, payload = {}) => {
    const response = await api.post(`/public/filas/${codigoPublico}/turnos`, payload)
    return response.data
  },
}

export default publicFilaService
