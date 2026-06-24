const TURNO_ACTIVO_KEY = 'dq_turno_activo'
const TURNO_ACTIVO_COOKIE = 'dq_turno_activo_token'
const COOKIE_MAX_AGE_SECONDS = 60 * 60 * 6

const ESTADOS_TERMINALES = [
  'FINALIZADO',
  'NO_PRESENTADO',
  'CANCELADO',
  'EXPIRADO',
]

const guardarCookieTurnoActivo = (tokenPublico) => {
  try {
    const secure = window.location.protocol === 'https:' ? '; Secure' : ''
    document.cookie = `${TURNO_ACTIVO_COOKIE}=${encodeURIComponent(tokenPublico)}; Max-Age=${COOKIE_MAX_AGE_SECONDS}; Path=/; SameSite=Lax${secure}`
  } catch {
    // La URL del turno sigue siendo la referencia si el navegador bloquea cookies.
  }
}

const obtenerCookieTurnoActivo = () => {
  try {
    const cookie = document.cookie
      .split('; ')
      .find((item) => item.startsWith(`${TURNO_ACTIVO_COOKIE}=`))

    if (!cookie) return null
    const token = decodeURIComponent(cookie.split('=').slice(1).join('='))
    return token || null
  } catch {
    return null
  }
}

const limpiarCookieTurnoActivo = () => {
  try {
    document.cookie = `${TURNO_ACTIVO_COOKIE}=; Max-Age=0; Path=/; SameSite=Lax`
  } catch {
    // Si no se puede limpiar la cookie, el backend validara si el turno sigue activo.
  }
}

export const guardarTurnoActivo = (tokenPublico, turno = {}) => {
  if (!tokenPublico) return

  if (ESTADOS_TERMINALES.includes(turno.estado)) {
    limpiarTurnoActivo(tokenPublico)
    return
  }

  guardarCookieTurnoActivo(tokenPublico)

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
    if (turno?.tokenPublico) return turno
  } catch {
    try {
      window.localStorage.removeItem(TURNO_ACTIVO_KEY)
    } catch {
      // No hay una sesión recuperable si el almacenamiento está bloqueado.
    }
  }

  const tokenCookie = obtenerCookieTurnoActivo()
  return tokenCookie ? { tokenPublico: tokenCookie } : null
}

export const limpiarTurnoActivo = (tokenPublico) => {
  const turnoActivo = obtenerTurnoActivo()

  if (!turnoActivo || !tokenPublico || turnoActivo.tokenPublico === tokenPublico) {
    limpiarCookieTurnoActivo()

    try {
      window.localStorage.removeItem(TURNO_ACTIVO_KEY)
    } catch {
      // La URL actual continúa siendo la referencia del turno.
    }
  }
}
