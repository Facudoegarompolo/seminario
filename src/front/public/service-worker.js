self.addEventListener('push', (event) => {
    if (!event.data) return

    const data = event.data.json()

    event.waitUntil(
        self.registration.showNotification(data.title, {
            body: data.body,
            icon: '/favicon.svg',
            badge: '/favicon.svg',

            // Intenta que quede visible hasta que el usuario interactúe
            requireInteraction: true,

            // Intenta vibrar en Android
            vibrate: [300, 100, 300, 100, 300],

            // Si llega otra notificación del mismo turno, vuelve a avisar
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

    event.waitUntil(
        clients.openWindow(`/estado/${tokenPublico}`)
    )
})