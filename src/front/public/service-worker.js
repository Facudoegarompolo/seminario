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
            icon: '/favicon.svg',
            badge: '/favicon.svg',

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
