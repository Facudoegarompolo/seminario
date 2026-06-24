import { crearStartUrlTurno } from './turnoLink'

let manifestUrlObject = null

const crearManifestTurno = (tokenPublico) => ({
  id: `/turno/${tokenPublico}`,
  name: 'Digital Queue',
  short_name: 'Digital Queue',
  description: 'Segui tu turno y recibi avisos cuando se acerque.',
  lang: 'es-AR',
  scope: '/',
  start_url: crearStartUrlTurno(tokenPublico),
  display: 'standalone',
  background_color: '#ffffff',
  theme_color: '#ffffff',
  icons: [
    {
      src: '/icon-192.png',
      sizes: '192x192',
      type: 'image/png',
    },
    {
      src: '/icon-512.png',
      sizes: '512x512',
      type: 'image/png',
    },
  ],
})

const esperarControlServiceWorker = async () => {
  if (navigator.serviceWorker.controller) return

  await new Promise((resolve) => {
    const timeout = window.setTimeout(resolve, 1500)

    navigator.serviceWorker.addEventListener(
      'controllerchange',
      () => {
        window.clearTimeout(timeout)
        resolve()
      },
      { once: true },
    )
  })
}

const obtenerLinkManifest = () => {
  let link = document.querySelector('link[rel="manifest"]')

  if (!link) {
    link = document.createElement('link')
    link.rel = 'manifest'
    document.head.appendChild(link)
  }

  return link
}

const aplicarManifestBlob = (tokenPublico) => {
  const blob = new Blob(
    [JSON.stringify(crearManifestTurno(tokenPublico))],
    { type: 'application/manifest+json' },
  )
  const siguienteManifestUrl = URL.createObjectURL(blob)

  obtenerLinkManifest().href = siguienteManifestUrl

  if (manifestUrlObject) {
    URL.revokeObjectURL(manifestUrlObject)
  }
  manifestUrlObject = siguienteManifestUrl
}

export const fijarUrlInstalacionTurno = (tokenPublico) => {
  if (!tokenPublico) return

  const startUrlTurno = crearStartUrlTurno(tokenPublico)
  const urlActual = `${window.location.pathname}${window.location.search}`

  if (urlActual !== startUrlTurno) {
    window.history.replaceState(window.history.state, '', startUrlTurno)
  }
}

export const prepararManifestTurno = async (tokenPublico) => {
  if (!tokenPublico) return

  aplicarManifestBlob(tokenPublico)

  if (!('serviceWorker' in navigator)) return

  await navigator.serviceWorker.register('/service-worker.js')
  await navigator.serviceWorker.ready
  await esperarControlServiceWorker()
}
