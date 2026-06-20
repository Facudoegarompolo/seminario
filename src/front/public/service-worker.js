self.addEventListener('push', (event) => {
    if (!event.data) return

    const data = event.data.json()

    event.waitUntil(
        self.registration.showNotification(data.title, {
            body: data.body,
            icon: '/favicon.svg',
            badge: '/favicon.svg',

            // Hace que la notificación sea más visible y no se cierre tan rápido
            requireInteraction: true,

            // Intenta vibrar en Android
            vibrate: [300, 100, 300, 100, 300],

            // Si llega otra del mismo tipo, vuelve a avisar
            tag: `turno-${data.turnoId}`,
            renotify: true,

            // Botones visibles en la notificación
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