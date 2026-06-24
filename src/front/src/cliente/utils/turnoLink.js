export const TURNO_QUERY_PARAM = 'turno'

export const obtenerTokenTurnoDesdeUrl = (search = window.location.search) => {
  try {
    const params = new URLSearchParams(search)
    const token = params.get(TURNO_QUERY_PARAM)
    return token?.trim() || null
  } catch {
    return null
  }
}

export const crearStartUrlTurno = (tokenPublico) => {
  const params = new URLSearchParams()
  params.set(TURNO_QUERY_PARAM, tokenPublico)
  return `/?${params.toString()}`
}
