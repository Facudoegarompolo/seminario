const ULTIMO_LOCAL_KEY = 'dq_ultimo_local'

export const guardarUltimoLocal = (codigoPublico, nombreLocal) => {
  if (!codigoPublico) return

  window.localStorage.setItem(
    ULTIMO_LOCAL_KEY,
    JSON.stringify({ codigoPublico, nombreLocal }),
  )
}

export const obtenerUltimoLocal = () => {
  try {
    const value = window.localStorage.getItem(ULTIMO_LOCAL_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    window.localStorage.removeItem(ULTIMO_LOCAL_KEY)
    return null
  }
}
