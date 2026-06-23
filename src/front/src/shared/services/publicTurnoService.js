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

  getPushPublicKey: async () => {
    const response = await api.get('/public/push/public-key')
    return response.data.publicKey
  },

  registrarPushSubscription: async (
    tokenPublico,
    subscription,
  ) => {
    const keys = subscription.toJSON().keys

    await api.post(
      `/public/turnos/${tokenPublico}/push-subscriptions`,
      {
        endpoint: subscription.endpoint,
        p256dh: keys.p256dh,
        auth: keys.auth,
      },
    )
  },

  recuperarTurnoActivo: async (subscription) => {
    const response = await api.post('/public/push-subscriptions/turno-activo', {
      endpoint: subscription.endpoint,
    })
    return response.data
  },

  desactivarPushSubscription: async (tokenPublico, subscription) => {
    await api.delete(`/public/turnos/${tokenPublico}/push-subscriptions`, {
      data: { endpoint: subscription.endpoint },
    })
  },
}

export default publicTurnoService
