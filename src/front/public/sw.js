console.log('SW cargado')

self.addEventListener('install', () => {
    console.log('SW INSTALADO')
})

self.addEventListener('activate', () => {
    console.log('SW ACTIVADO')
})

self.addEventListener('push', (event) => {

    console.log('PUSH RECIBIDA')

    let data = {
        title: 'Digital Queue',
        body: 'Nueva notificación'
    }

    if (event.data) {
        try {
            data = event.data.json()
        } catch (error) {
            console.error('Error parseando payload', error)
        }
    }

    event.waitUntil(

        self.registration.showNotification(data.title, {

            body: data.body,

            icon: '/favicon.svg',

            badge: '/favicon.svg',

            vibrate: [500, 200, 500],

            requireInteraction: true,

            renotify: true,

            silent: false,

            tag: 'digital-queue-turno'
        })
    )
})