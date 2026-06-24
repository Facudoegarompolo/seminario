import { crearStartUrlTurno } from './turnoLink'

const MANIFEST_TURNO_PATH = '/turno-manifest.webmanifest'

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

export const prepararManifestTurno = async (tokenPublico) => {
  if (!tokenPublico || !('serviceWorker' in navigator)) return

  await navigator.serviceWorker.register('/service-worker.js')
  await navigator.serviceWorker.ready
  await esperarControlServiceWorker()

  const params = new URLSearchParams()
  params.set('turno', tokenPublico)
  params.set('startUrl', crearStartUrlTurno(tokenPublico))

  obtenerLinkManifest().href = `${MANIFEST_TURNO_PATH}?${params.toString()}`
}
