const TURNO_ACTIVO_KEY = 'dq_turno_activo'

const ESTADOS_TERMINALES = [
  'FINALIZADO',
  'NO_PRESENTADO',
  'CANCELADO',
  'EXPIRADO',
]

export const guardarTurnoActivo = (tokenPublico, turno = {}) => {
  if (!tokenPublico) return

  if (ESTADOS_TERMINALES.includes(turno.estado)) {
    limpiarTurnoActivo(tokenPublico)
    return
  }

  try {
    window.localStorage.setItem(
      TURNO_ACTIVO_KEY,
      JSON.stringify({
        tokenPublico,
        codigoPublico: turno.codigoPublico,
        nombreLocal: turno.nombreLocal,
      }),
    )
  } catch {
    // El token sigue presente en la URL aunque iOS bloquee el almacenamiento.
  }
}

export const obtenerTurnoActivo = () => {
  try {
    const value = window.localStorage.getItem(TURNO_ACTIVO_KEY)
    const turno = value ? JSON.parse(value) : null
    return turno?.tokenPublico ? turno : null
  } catch {
    try {
      window.localStorage.removeItem(TURNO_ACTIVO_KEY)
    } catch {
      // No hay una sesión recuperable si el almacenamiento está bloqueado.
    }
    return null
  }
}

export const limpiarTurnoActivo = (tokenPublico) => {
  const turnoActivo = obtenerTurnoActivo()

  if (!turnoActivo || !tokenPublico || turnoActivo.tokenPublico === tokenPublico) {
    try {
      window.localStorage.removeItem(TURNO_ACTIVO_KEY)
    } catch {
      // La URL actual continúa siendo la referencia del turno.
    }
  }
}
