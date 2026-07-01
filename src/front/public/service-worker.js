self.addEventListener('install', (event) => {
    event.waitUntil(self.skipWaiting())
})

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim())
})

self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url)

    if (url.origin !== self.location.origin || url.pathname !== '/turno-manifest.webmanifest') {
        return
    }

    const tokenPublico = url.searchParams.get('turno')
    const startUrl = url.searchParams.get('startUrl') || (tokenPublico ? `/?turno=${encodeURIComponent(tokenPublico)}` : '/')

    event.respondWith(new Response(JSON.stringify({
        id: tokenPublico ? `/turno/${tokenPublico}` : '/',
        name: 'Digital Queue',
        short_name: 'DQ',
        description: 'Segui tu turno y recibi avisos cuando se acerque.',
        lang: 'es-AR',
        scope: '/',
        start_url: startUrl,
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
    }), {
        headers: {
            'Content-Type': 'application/manifest+json',
            'Cache-Control': 'no-store',
        },
    }))
})

self.addEventListener('push', (event) => {
    if (!event.data) return

    let data
    try {
        data = event.data.json()
    } catch {
        data = {
            title: 'Digital Queue',
            body: event.data.text(),
        }
    }

    event.waitUntil(
        self.registration.showNotification(data.title, {
            body: data.body,
            icon: '/icon-192.png',
            badge: '/icon-192.png',

            requireInteraction: true,
            vibrate: [300, 100, 300, 100, 300],
            tag: `turno-${data.turnoId}`,
            renotify: true,
            actions: [
                {
                    action: 'ver-turno',
                    title: 'Ver turno',
                },
            ],

            data: {
                tokenPublico: data.tokenPublico,
            },
        }),
    )
})

self.addEventListener('notificationclick', (event) => {
    event.notification.close()

    const tokenPublico = event.notification.data?.tokenPublico

    if (!tokenPublico) return

    const urlTurno = `/turno/${tokenPublico}`

    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true })
            .then(async (windowClients) => {
                const clienteAbierto = windowClients.find(
                    (client) => new URL(client.url).pathname === urlTurno
                )

                if (clienteAbierto) return clienteAbierto.focus()
                return self.clients.openWindow(urlTurno)
            })
    )
})
