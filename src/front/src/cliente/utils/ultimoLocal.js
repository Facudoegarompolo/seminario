const ULTIMO_LOCAL_KEY = 'dq_ultimo_local'

export const guardarUltimoLocal = (codigoPublico, nombreLocal) => {
  if (!codigoPublico) return

  try {
    window.localStorage.setItem(
      ULTIMO_LOCAL_KEY,
      JSON.stringify({ codigoPublico, nombreLocal }),
    )
  } catch {
    // La navegación debe seguir funcionando aunque iOS bloquee el almacenamiento.
  }
}

export const obtenerUltimoLocal = () => {
  try {
    const value = window.localStorage.getItem(ULTIMO_LOCAL_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    try {
      window.localStorage.removeItem(ULTIMO_LOCAL_KEY)
    } catch {
      // No hay nada más que recuperar si el almacenamiento está bloqueado.
    }
    return null
  }
}
